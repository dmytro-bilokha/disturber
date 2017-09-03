package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.JoinedRoomDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import com.dmytrobilokha.disturber.network.dto.TimelineDto;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by dimon on 13.08.17.
 */
public class MatrixClient extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixClient.class);

    //This list is bound to UI and should be accessed only from the FX application thread
    private final List<String> messageList;

    private final MatrixAccount matrixAccount;
    private final MatrixApiConnector apiConnector;


    MatrixClient(List<String> messageList, MatrixAccount matrixAccount, MatrixApiConnector apiConnector) {
        this.messageList = messageList;
        this.matrixAccount = matrixAccount;
        this.apiConnector = apiConnector;
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            switch (matrixAccount.getState()) {
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
            Platform.runLater(this::updateUiMessageList);
            Thread.sleep(matrixAccount.getNetworkConnectionConfig().getConnectionInterval());
        }
        return null;
    }

    //The method to update FX message list, should be called only from FX application thread
    private void updateUiMessageList() {
        String message;
        while ((message = matrixAccount.pollMessage()) != null) {
           messageList.add(message);
        }
    }

    private void connect() {
        String baseUrl = matrixAccount.getAccountConfig().getServerAddress();
        apiConnector.createConnection(baseUrl);
        matrixAccount.setState(MatrixAccount.State.CONNECTED);
    }

    private void login() {
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin(matrixAccount.getAccountConfig().getLogin());
        loginPasswordDto.setPassword(matrixAccount.getAccountConfig().getPassword());
        LoginAnswerDto answerDto;
        try {
            answerDto = apiConnector.issueRequest(matrixService -> matrixService.login(loginPasswordDto));
        } catch (ApiConnectException ex) {
            LOG.error("Failed to login with {}", matrixAccount.getAccountConfig(), ex);
            matrixAccount.addMessage("Failed to login, because of input/output error");
            return;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to login with {}, got {}", matrixAccount.getAccountConfig(), ex.getApiError(), ex);
            matrixAccount.addMessage("Failed to log in with " + ex.getApiError());
            return;
        }
        matrixAccount.setAccessToken(answerDto.getAccessToken());
        matrixAccount.setHomeServer(answerDto.getHomeServer());
        matrixAccount.setUserId(answerDto.getUserId());
        matrixAccount.addMessage("Successfully logged in. Token=" + matrixAccount.getAccessToken());
        matrixAccount.setState(MatrixAccount.State.LOGGEDIN);
    }

    private void initialSync() {
       boolean success = sync(matrixService -> matrixService.sync(matrixAccount.getAccessToken()));
       if (success)
           matrixAccount.setState(MatrixAccount.State.INITIAL_SYNCED);
    }

    private void stepSync() {
        sync(matrixService -> matrixService.sync(matrixAccount.getAccessToken(), matrixAccount.getNextBatchId(), 3000));
    }

    private boolean sync(Function<MatrixService, Call<SyncResponseDto>> requestFunction) {
        SyncResponseDto syncResponseDto;
        try {
            syncResponseDto = apiConnector.issueRequest(requestFunction);
        } catch (ApiConnectException ex) {
            LOG.error("Failed to synchronize {} with server because of input/output error", matrixAccount.getAccountConfig(), ex);
            matrixAccount.addMessage("Failed to sync because of IOException");
            return false;
        } catch (ApiRequestException ex) {
            LOG.error("Failed to synchronize {} with server, got {}", matrixAccount.getAccountConfig(), ex.getApiError(), ex);
            matrixAccount.addMessage("Failed to sync with " + ex.getApiError());
            return false;
        }
        matrixAccount.setNextBatchId(syncResponseDto.getNextBatch());
        List<String> newMessages = extractNewMessages(syncResponseDto);
        matrixAccount.addMessages(newMessages);
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

}
