package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.JoinedRoomDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import com.dmytrobilokha.disturber.network.dto.TimelineDto;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by dimon on 13.08.17.
 */
public class MatrixClient extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixClient.class);

    private final AccountConfig accountConfig;
    private final MatrixEventQueue eventQueue;
    private final MatrixApiConnector apiConnector;

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
            addMessage("Failed to login, because of input/output error");
            return;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to login with {}, got {}", accountConfig, ex.getApiError(), ex);
            addMessage("Failed to log in with " + ex.getApiError());
            return;
        }
        accessToken = answerDto.getAccessToken();
        homeServer = answerDto.getHomeServer();
        userId = answerDto.getUserId();
        state = State.LOGGEDIN;
        addMessage("Successfully logged in. Token=" + accessToken);
    }

    private void addMessage(String message) {
        eventQueue.addEvent(message);
        haveNewEvents = true;
    }

    private void addMessages(Collection<String> messages) {
        if (messages.isEmpty())
            return;
        eventQueue.addEvents(messages);
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
            addMessage("Failed to sync because of IOException");
            return false;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to synchronize {} with server, got {}", accountConfig, ex.getApiError(), ex);
            addMessage("Failed to sync with " + ex.getApiError());
            return false;
        }
        nextBatchId = syncResponseDto.getNextBatch();
        List<String> newMessages = extractNewMessages(syncResponseDto);
        addMessages(newMessages);
        return true;
    }

    private List<String> extractNewMessages(SyncResponseDto syncResponseDto) {
        Map<String, JoinedRoomDto> joinedRoomMap = syncResponseDto.getRooms().getJoinedRoomMap();
        List<String> messagesList = new ArrayList<>();
        for (JoinedRoomDto joinedRoomDto : joinedRoomMap.values()) {
            TimelineDto timelineDto = joinedRoomDto.getTimeline();
            List<EventDto> eventDtos = timelineDto.getEvents();
            eventDtos.stream()
                    .map(EventDto::getContent)
                    .filter(content -> content.getBody() != null)
                    .map(content -> "type:" + content.getMsgType() + " body:" + content.getBody())
                    .forEach(messagesList::add);
        }
        return messagesList;
    }

    public enum State {
        NOT_CONNECTED, CONNECTED, LOGGEDIN, INITIAL_SYNCED;
    }

}
