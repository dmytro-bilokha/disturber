package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.JoinedRoomDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The class represents Matrix client. It contains logic to do login, synchronization, etc.
 */
class MatrixClient extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixClient.class);
    private static final String SYSTEM = "SYSTEM";
    private static final String TEXT_CONTENT = "m.text";

    private final AccountConfig accountConfig;
    private final MatrixEventQueue eventQueue;
    private final MatrixApiConnector apiConnector;
    private final MatrixEvent.Builder eventBuilder;

    private State state = State.NOT_CONNECTED;
    private String accessToken;
    private String homeServer;
    private String userId;
    private String nextBatchId;
    private boolean haveNewEvents;

    MatrixClient(AccountConfig accountConfig, MatrixEventQueue eventQueue, MatrixApiConnector apiConnector) {
        this.accountConfig = accountConfig;
        this.eventQueue = eventQueue;
        this.apiConnector = apiConnector;
        this.eventBuilder = MatrixEvent.newBuilder();
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
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
                    stepSync();
                    break;
            }
            if (haveNewEvents)
                eventQueue.triggerEventCallback();
            Thread.sleep(accountConfig.getBetweenSyncPause());
        }
        return null;
    }

    private void connect() {
        apiConnector.createConnection(accountConfig.getServerAddress(), accountConfig.getNetworkTimeout());
        state = State.CONNECTED;
    }

    private void login() {
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin(accountConfig.getLogin());
        loginPasswordDto.setPassword(accountConfig.getPassword());
        LoginAnswerDto answerDto;
        try {
            answerDto = apiConnector.issueRequest(matrixService -> matrixService.login(loginPasswordDto));
        } catch (ApiConnectException ex) {
            LOG.error("Failed to login with {}", accountConfig, ex);
            sendSystemMessage("Failed to login, because of input/output error");
            return;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to login with {}, got {}", accountConfig, ex.getApiError(), ex);
            sendSystemMessage("Failed to log in with " + ex.getApiError());
            return;
        }
        accessToken = answerDto.getAccessToken();
        homeServer = answerDto.getHomeServer();
        userId = answerDto.getUserId();
        state = State.LOGGEDIN;
        sendSystemMessage("Successfully logged in. Token=" + accessToken);
    }

    private void sendSystemMessage(String message) {
        eventQueue.addEvent(eventBuilder
                .userId(userId == null ? buildUserId() : userId)
                .roomId(SYSTEM)
                .sender(SYSTEM)
                .contentType(TEXT_CONTENT)
                .content(message)
                .build());
        haveNewEvents = true;
    }

    private String buildUserId() {
        try {
            return '@' + accountConfig.getLogin() + ':' + new URL(accountConfig.getServerAddress()).getHost();
        } catch (MalformedURLException ex) {
            LOG.error("{} contains invalid server URL", accountConfig.getServerAddress(), ex);
            return '@' + accountConfig.getLogin() + ':' + accountConfig.getServerAddress();
        }
    }

    private void addEventsToQueue(Collection<MatrixEvent> events) {
        if (events.isEmpty())
            return;
        eventQueue.addEvents(events);
        haveNewEvents = true;
    }

    private void initialSync() {
       boolean success = sync(matrixService -> matrixService.sync(accessToken));
       if (success)
           state = State.INITIAL_SYNCED;
    }

    private void stepSync() {
        sync(matrixService -> matrixService.sync(accessToken, nextBatchId, accountConfig.getSyncTimeout()));
    }

    private boolean sync(Function<MatrixService, Call<SyncResponseDto>> requestFunction) {
        SyncResponseDto syncResponseDto;
        try {
            syncResponseDto = apiConnector.issueRequest(requestFunction);
        } catch (ApiConnectException ex) {
            LOG.error("Failed to synchronize {} with server because of input/output error", accountConfig, ex);
            sendSystemMessage("Failed to sync because of IOException");
            return false;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to synchronize {} with server, got {}", accountConfig, ex.getApiError(), ex);
            sendSystemMessage("Failed to sync with " + ex.getApiError());
            return false;
        }
        nextBatchId = syncResponseDto.getNextBatch();
        List<MatrixEvent> events = extractEvents(syncResponseDto);
        addEventsToQueue(events);
        return true;
    }

    private List<MatrixEvent> extractEvents(SyncResponseDto syncResponseDto) {
        Map<String, JoinedRoomDto> joinedRoomMap = syncResponseDto.getRooms().getJoinedRoomMap();
        List<MatrixEvent> messagesList = new ArrayList<>();
        eventBuilder.userId(userId);
        for (Map.Entry<String, JoinedRoomDto> joinedRoomDtoEntry : joinedRoomMap.entrySet()) {
            eventBuilder.roomId(joinedRoomDtoEntry.getKey());
            List<EventDto> eventDtos = joinedRoomDtoEntry.getValue().getTimeline().getEvents();
            for (EventDto eventDto : eventDtos) {
                messagesList.add(eventBuilder.serverTimestamp(eventDto.getServerTimestamp())
                            .sender(eventDto.getSender())
                            .contentType(eventDto.getContent().getMsgType())
                            .content(eventDto.getContent().getBody())
                            .build());
            }
        }
        return messagesList;
    }

    private enum State {
        NOT_CONNECTED, CONNECTED, LOGGEDIN, INITIAL_SYNCED;
    }

}
