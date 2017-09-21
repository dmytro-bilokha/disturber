package com.dmytrobilokha.disturber.network;

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
//TODO: add error handling with retry and pauses between, etc
class MatrixSynchronizer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixSynchronizer.class);

    private final Queue<OutgoingMessage> appToServerQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong messageId = new AtomicLong();
    private final AccountConfig accountConfig;
    private final CrossThreadEventQueue serverToAppQueue;
    private final MatrixApiConnector apiConnector;
    private final MatrixEvent.Builder matrixEventBuilder = MatrixEvent.newBuilder();

    private State state = State.NOT_CONNECTED;
    private String accessToken;
    private String nextBatchId;
    private boolean haveNewEvents;
    private volatile boolean keepGoing = true;

    MatrixSynchronizer(AccountConfig accountConfig, CrossThreadEventQueue serverToAppQueue, MatrixApiConnector apiConnector) {
        this.accountConfig = accountConfig;
        this.serverToAppQueue = serverToAppQueue;
        this.apiConnector = apiConnector;
    }

    @Override
    public void run() {
        while (keepGoing) {
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

                case INITIAL_SYNCED:
                    sendEventsToServer();
                    stepSync();
                    break;
            }
            if (haveNewEvents) {
                serverToAppQueue.triggerEventCallback();
                haveNewEvents = false;
            }
            if (!keepGoing) //Check keepGoing flag once again to make app more responsive in case of shut down
                break;
            try {
                Thread.sleep(accountConfig.getBetweenSyncPause());
            } catch (InterruptedException ex) {
                LOG.error("Thread interrupted", ex);
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    //This method to be called from the main FX application thread to "stop the show"
    public void disconnect() {
        keepGoing = false;
    }

    private void connect() {
        apiConnector.createConnection(accountConfig.getServerAddress()
                , accountConfig.getNetworkTimeout(), accountConfig.getProxyServer());
        state = State.CONNECTED;
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
            addEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGIN_CONNECTION_FAILED, accountConfig));
            return;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to login with {}, got {}", accountConfig, ex.getApiError(), ex);
            addEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGIN_FAILED, accountConfig));
            return;
        }
        accessToken = answerDto.getAccessToken();
        String userId = answerDto.getUserId();
        if (!accountConfig.getUserId().equals(userId))
            LOG.warn("After logging in for {} got from server userId={}, but from account have userId={}"
                        , accountConfig, userId, accountConfig.getUserId());
        state = State.LOGGEDIN;
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
        boolean success = sync(connector -> connector.sync(accessToken));
        if (success)
            state = State.INITIAL_SYNCED;
    }

    private void stepSync() {
        sync(matrixService -> matrixService.sync(accessToken, nextBatchId, accountConfig.getSyncTimeout()));
    }

    private boolean sync(ThrowingFunction<MatrixApiConnector, SyncResponseDto> requestFunction) {
        SyncResponseDto syncResponseDto;
        try {
            syncResponseDto = requestFunction.apply(apiConnector);
        } catch (ApiConnectException ex) {
            LOG.error("Failed to synchronize {} with server because of input/output error", accountConfig, ex);
            addEvent(AppEvent.withClassifier(AppEventType.MATRIX_SYNC_CONNECTION_FAILED, accountConfig.getUserId()));
            return false;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to synchronize {} with server, got {}", accountConfig, ex.getApiError(), ex);
            addEvent(AppEvent.withClassifier(AppEventType.MATRIX_SYNC_FAILED, accountConfig.getUserId()));
            return false;
        } catch (Exception ex) {
            LOG.error("Failed to synchronize {} with server, got unexpected exception", accountConfig, ex);
            addEvent(AppEvent.withClassifier(AppEventType.MATRIX_SYNC_CONNECTION_FAILED, accountConfig.getUserId()));
            return false;
        }
        nextBatchId = syncResponseDto.getNextBatch();
        List<AppEvent> events = mapSyncResponseDtoToAppEvents(syncResponseDto);
        addEvents(events);
        return true;
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
        OutgoingMessage message = null;
        try {
            while ((message = appToServerQueue.poll()) != null) {
                EventContentDto messageContent = new EventContentDto();
                messageContent.setBody(message.getMessageText());
                messageContent.setMsgType("m.text");
                apiConnector.sendMessageEvent(accessToken, message.getRoomId()
                        , "m.room.message", message.getLocalId(), messageContent);
            }
        } catch (ApiConnectException | ApiRequestException ex) {
            LOG.error("Failed to send message {}", message);
        }
    }

    public void enqueueOutgoingMessage(String roomId, String message) {
        OutgoingMessage outgoingMessage = new OutgoingMessage(roomId, message, getNewMessageId());
        appToServerQueue.add(outgoingMessage);
    }

    private String getNewMessageId() {
        return String.valueOf(messageId.getAndIncrement());
    }

    private enum State {
        NOT_CONNECTED, CONNECTED, LOGGEDIN, INITIAL_SYNCED;
    }

}
