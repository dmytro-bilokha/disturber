package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.SystemMessage;
import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.network.dto.EventContentDto;
import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.JoinedRoomDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import com.dmytrobilokha.disturber.util.ThrowingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The class represents synchronizer which should be run in the separate thread and synchronizes matrix events for
 * given account.
 */
//TODO: add tests on pause increasing on fail and on error/retry behavior
class MatrixSynchronizer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixSynchronizer.class);
    private static final int MAX_TRY = 5;

    private final Queue<OutgoingMessage> appToServerQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong messageId = new AtomicLong();
    private final AccountConfig accountConfig;
    private final CrossThreadEventQueue serverToAppQueue;
    private final MatrixApiConnector apiConnector;
    private final ApiExceptionToSystemMessageConverter exceptionConverter;
    private final MatrixEvent.Builder matrixEventBuilder = MatrixEvent.newBuilder();

    private State state = State.NOT_CONNECTED;
    private boolean haveNewEvents;
    private String accessToken;
    private String nextBatchId;
    private volatile int networkTryNumber = 0;
    private volatile boolean active = true;
    private volatile boolean keepGoing = true;

    MatrixSynchronizer(AccountConfig accountConfig, CrossThreadEventQueue serverToAppQueue
            , MatrixApiConnector apiConnector, ApiExceptionToSystemMessageConverter exceptionConverter) {
        this.accountConfig = accountConfig;
        this.serverToAppQueue = serverToAppQueue;
        this.apiConnector = apiConnector;
        this.exceptionConverter = exceptionConverter;
        setName(this.getClass().getSimpleName() + "-" + accountConfig.getUserId());
    }

    @Override
    public void run() {
        while (keepGoing) {
            talkToServer();
            pushServerEventsToApp();
            try {
                sleepPauseTime();
            } catch (InterruptedException ex) {
                LOG.error("Thread interrupted", ex);
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void talkToServer() {
        if (!active) //If active flag is not set, do nothing, will just sleep all the time
            return;
        switch (state) {
            case NOT_CONNECTED:
                connect();
                break;

            case CONNECTED:
                login();
                break;

            case LOGGEDIN:
                initialSync();
                break;

            case SYNCED:
                sendEventsToServer();
                break;

            case MESSAGES_SENT:
                stepSync();
                break;
        }
    }

    private void pushServerEventsToApp() {
        if (!haveNewEvents)
            return;
        serverToAppQueue.triggerEventCallback();
        haveNewEvents = false;
    }

    private void sleepPauseTime() throws InterruptedException {
        Thread.sleep(accountConfig.getBetweenSyncPause() * (long) networkTryNumber); //Pause increases on fail to make server's life easier
    }

    //This method to be called from the main FX application thread to recover after fail if user wants
    public void setRetryOn() {
        networkTryNumber = 1;
        active = true;
    }

    //This method to be called from the main FX application thread to "stop the show"
    public void disconnect() {
        keepGoing = false;
    }

    private void connect() {
        apiConnector.createConnection(accountConfig.getServerAddress()
                , accountConfig.getNetworkTimeout(), accountConfig.getProxyServer());
        handleChangeState(State.CONNECTED);
    }

    private void handleChangeState(State state) {
        networkTryNumber = 1;
        this.state = state;
    }

    private void handleNetworkFail(State state, Exception ex) {
        networkTryNumber++;
        if (networkTryNumber <= MAX_TRY)
            return;
        active = false;
        SystemMessage systemMessage = exceptionConverter.buildSystemMessage(state, accountConfig.getUserId(), ex);
        addEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_CONNECTION_FAILED, accountConfig, systemMessage));
    }

    private void handleResponseFail(State state, Exception ex) {
        active = false;
        SystemMessage systemMessage = exceptionConverter.buildSystemMessage(state, accountConfig.getUserId(), ex);
        addEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_RESPONSE_FAILED, accountConfig, systemMessage));
    }

    private void login() {
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin(accountConfig.getLogin());
        loginPasswordDto.setPassword(accountConfig.getPassword());
        LoginAnswerDto answerDto;
        try {
            answerDto = apiConnector.login(loginPasswordDto);
        } catch (ApiConnectException ex) {
            LOG.error("Failed to login with {}", accountConfig, ex);
            handleNetworkFail(State.LOGGEDIN, ex);
            return;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to login with {}, got {}", accountConfig, ex.getApiError(), ex);
            handleResponseFail(State.LOGGEDIN, ex);
            return;
        }
        accessToken = answerDto.getAccessToken();
        String userId = answerDto.getUserId();
        if (!accountConfig.getUserId().equals(userId))
            LOG.warn("After logging in for {} got from server userId={}, but from account have userId={}"
                        , accountConfig, userId, accountConfig.getUserId());
        handleChangeState(State.LOGGEDIN);
        addEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, userId));
    }

    private void addEvent(AppEvent event) {
        serverToAppQueue.addEvent(event);
        haveNewEvents = true;
    }

    private void addEvents(Collection<AppEvent> events) {
        if (events.isEmpty())
            return;
        serverToAppQueue.addEvents(events);
        haveNewEvents = true;
    }

    private void initialSync() {
        sync(connector -> connector.sync(accessToken));
    }

    private void stepSync() {
        sync(connector -> connector.sync(accessToken, nextBatchId, accountConfig.getSyncTimeout()));
    }

    private void sync(ThrowingFunction<MatrixApiConnector, SyncResponseDto> requestFunction) {
        SyncResponseDto syncResponseDto;
        try {
            syncResponseDto = requestFunction.apply(apiConnector);
        } catch (ApiConnectException ex) {
            LOG.error("Failed to synchronize {} with server because of input/output error", accountConfig, ex);
            handleNetworkFail(State.SYNCED, ex);
            return;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to synchronize {} with server, got {}", accountConfig, ex.getApiError(), ex);
            handleResponseFail(State.SYNCED, ex);
            return;
        } catch (Exception ex) {
            LOG.error("Failed to synchronize {} with server, got unexpected exception", accountConfig, ex);
            handleResponseFail(State.SYNCED, ex);
            return;
        }
        nextBatchId = syncResponseDto.getNextBatch();
        List<AppEvent> events = mapSyncResponseDtoToAppEvents(syncResponseDto);
        addEvents(events);
        handleChangeState(State.SYNCED);
    }

    private List<AppEvent> mapSyncResponseDtoToAppEvents(SyncResponseDto syncResponseDto) {
        Map<String, JoinedRoomDto> joinedRoomMap = syncResponseDto.getRooms().getJoinedRoomMap();
        List<AppEvent> appEvents = new ArrayList<>();
        for (Map.Entry<String, JoinedRoomDto> joinedRoomDtoEntry : joinedRoomMap.entrySet()) {
            RoomKey roomKey = new RoomKey(accountConfig.getUserId(), joinedRoomDtoEntry.getKey());
            List<EventDto> eventDtos = joinedRoomDtoEntry.getValue().getTimeline().getEvents();
            for (EventDto eventDto : eventDtos) {
                MatrixEvent matrixEvent = matrixEventBuilder
                        .serverTimestamp(eventDto.getServerTimestamp())
                        .sender(eventDto.getSender())
                        .contentType(eventDto.getContent().getMsgType())
                        .content(eventDto.getContent().getBody())
                        .build();
                appEvents.add(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_NEW_EVENT_GOT, roomKey, matrixEvent));
            }
        }
        return appEvents;
    }

    private void sendEventsToServer() {
        OutgoingMessage message;
        while ((message = appToServerQueue.peek()) != null) {
            EventContentDto messageContent = new EventContentDto();
            messageContent.setBody(message.getMessageText());
            messageContent.setMsgType("m.text");
            try {
                apiConnector.sendMessageEvent(accessToken, message.getRoomId()
                        , "m.room.message", message.getLocalId(), messageContent);
            } catch (ApiConnectException ex) {
                LOG.error("Failed to send message {}, because of input/output error", message, ex);
                handleNetworkFail(State.MESSAGES_SENT, ex);
                return;
            } catch (ApiRequestException ex) {
                LOG.error("Failed to send message {}, got {}", message, ex.getApiError(), ex);
                handleResponseFail(State.MESSAGES_SENT, ex);
                return;
            }
            appToServerQueue.poll();
        }
        handleChangeState(State.MESSAGES_SENT);
    }

    //Called from JavaFX application thread
    public void enqueueOutgoingMessage(String roomId, String message) {
        OutgoingMessage outgoingMessage = new OutgoingMessage(roomId, message, getNewMessageId());
        appToServerQueue.add(outgoingMessage);
    }

    private String getNewMessageId() {
        return String.valueOf(messageId.getAndIncrement());
    }

    enum State {
        NOT_CONNECTED, CONNECTED, LOGGEDIN, SYNCED, MESSAGES_SENT;
    }

}
