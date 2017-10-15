package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.SystemMessage;
import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.network.dto.EventContentDto;
import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.InvitedRoomDto;
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
class MatrixSynchronizer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixSynchronizer.class);
    private static final int MAX_WAIT_MULTIPLIER = 5;
    private static final String INVITE = "invite";
    private static final String ROOM_MEMBER_TYPE = "m.room.member";
    private static final String ROOM_MESSAGE_TYPE = "m.room.message";
    private static final String TEXT_MESSAGE = "m.text";

    private final Queue<OutgoingMessage> appToServerQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong messageId = new AtomicLong();
    private final AccountConfig accountConfig;
    private final CrossThreadEventQueue serverToAppQueue;
    private final MatrixApiConnector apiConnector;
    private final ApiExceptionToSystemMessageConverter exceptionConverter;
    private final MatrixEvent.Builder matrixEventBuilder = MatrixEvent.newBuilder();
    private final ConnectionRetryStateHolder retryStateHolder;

    private State state = State.NOT_CONNECTED;
    private boolean haveNewEvents;
    private String accessToken;
    private String nextBatchId;
    private volatile boolean keepGoing = true;

    MatrixSynchronizer(AccountConfig accountConfig, CrossThreadEventQueue serverToAppQueue
            , MatrixApiConnector apiConnector, ApiExceptionToSystemMessageConverter exceptionConverter) {
        this.accountConfig = accountConfig;
        this.serverToAppQueue = serverToAppQueue;
        this.apiConnector = apiConnector;
        this.exceptionConverter = exceptionConverter;
        setName(this.getClass().getSimpleName() + "-" + accountConfig.getUserId());
        this.retryStateHolder = new ConnectionRetryStateHolder(accountConfig.getBetweenSyncPause());
    }

    @Override
    public void run() {
        while (keepGoing) {
            try {
                sleepPauseTime();
            } catch (InterruptedException ex) {
                LOG.error("Thread interrupted", ex);
                Thread.currentThread().interrupt();
                return;
            }
            talkToServer();
            pushServerEventsToApp();
        }
    }

    private void talkToServer() {
        if (!retryStateHolder.isActive()) //If active flag is not set, do nothing, will just sleep all the time
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
        Thread.sleep(retryStateHolder.pauseTime()); //Pause increases on fail to make server's life easier
    }

    //This method to be called from the main FX application thread to recover after fail if user wants
    public void setRetryOn() {
        retryStateHolder.reset();
    }

    //This method to be called from the main FX application thread to "stop the show"
    public void disconnect() {
        retryStateHolder.stop();
        keepGoing = false;
    }

    private void connect() {
        apiConnector.createConnection(accountConfig.getServerAddress()
                , accountConfig.getNetworkTimeout(), accountConfig.getProxyServer());
        handleChangeState(State.CONNECTED);
    }

    private void handleChangeState(State state) {
        retryStateHolder.reset();
        this.state = state;
    }

    private void handleNetworkFail(State state, Exception ex) {
        SystemMessage systemMessage = exceptionConverter.buildSystemMessage(state, accountConfig.getUserId(), ex);
        if (retryStateHolder.getTotalWaitTime() < MAX_WAIT_MULTIPLIER * accountConfig.getNetworkTimeout()) {
            addEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_CONNECTION_ISSUE, accountConfig, systemMessage));
        } else {
            retryStateHolder.stop();
            addEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_CONNECTION_FAILED, accountConfig, systemMessage));
        }
    }

    private void handleResponseFail(State state, Exception ex) {
        retryStateHolder.stop();
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
        handleChangeState(State.SYNCED);
        addEvent(AppEvent.withClassifier(AppEventType.MATRIX_SYNCED, accountConfig.getUserId()));
        List<AppEvent> events = mapSyncResponseDtoToAppEvents(syncResponseDto);
        addEvents(events);
    }

    private List<AppEvent> mapSyncResponseDtoToAppEvents(SyncResponseDto syncResponseDto) {
        List<AppEvent> appEvents = new ArrayList<>();
        appEvents.addAll(mapJoinedRoomEvents(syncResponseDto.getRooms().getJoinedRoomMap()));
        appEvents.addAll(mapInvitedRoomEvents(syncResponseDto.getRooms().getInvitedRoomMap()));
        return appEvents;
    }

    private List<AppEvent> mapJoinedRoomEvents(Map<String, JoinedRoomDto> joinedRoomMap) {
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

    private  List<AppEvent> mapInvitedRoomEvents(Map<String, InvitedRoomDto> invitedRoomMap) {
        List<AppEvent> appEvents = new ArrayList<>();
        for (Map.Entry<String, InvitedRoomDto> invitedRoomDtoEntry : invitedRoomMap.entrySet()) {
            RoomKey roomKey = new RoomKey(accountConfig.getUserId(), invitedRoomDtoEntry.getKey());
            List<EventDto> eventDtos = invitedRoomDtoEntry.getValue().getInviteState().getEvents();
            for (EventDto eventDto : eventDtos) {
                if (!ROOM_MEMBER_TYPE.equals(eventDto.getType()) || !INVITE.equals(eventDto.getContent().getMembership()))
                    continue; //Skip all events which are not invites
                MatrixEvent matrixEvent = matrixEventBuilder
                        .serverTimestamp(eventDto.getServerTimestamp())
                        .sender(eventDto.getSender())
                        .build();
                appEvents.add(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_NEW_INVITE_GOT, roomKey, matrixEvent));
            }
        }
        return appEvents;
    }

    private void sendEventsToServer() {
        OutgoingMessage message;
        while ((message = appToServerQueue.peek()) != null) {
            EventContentDto messageContent = new EventContentDto();
            messageContent.setBody(message.getMessageText());
            messageContent.setMsgType(TEXT_MESSAGE);
            try {
                apiConnector.sendMessageEvent(accessToken, message.getRoomId()
                        , ROOM_MESSAGE_TYPE, message.getLocalId(), messageContent);
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
