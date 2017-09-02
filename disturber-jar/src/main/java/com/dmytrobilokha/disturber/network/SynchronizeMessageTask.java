package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.connection.NetworkConnectionConfig;
import com.dmytrobilokha.disturber.network.dto.ErrorDto;
import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.JoinedRoomDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import com.dmytrobilokha.disturber.network.dto.TimelineDto;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageTask extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizeMessageTask.class);

    //This list is bound to UI and should be accessed only from the FX application thread
    private final List<String> messageList;

    private final MatrixAccount matrixAccount;

    private MatrixService matrixService;
    private Converter<ResponseBody, ErrorDto> errorConverter;

    SynchronizeMessageTask(List<String> messageList, MatrixAccount matrixAccount) {
        this.messageList = messageList;
        this.matrixAccount = matrixAccount;
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
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .client(buildConfiguredHttpClient(matrixAccount.getNetworkConnectionConfig()))
                    .build();
            matrixService = retrofit.create(MatrixService.class);
            errorConverter = retrofit.responseBodyConverter(ApiError.class, new Annotation[0]);
            matrixAccount.setState(MatrixAccount.State.CONNECTED);
        } catch (Exception ex) {
            LOG.error("Failed to open connection to the endpoint '{}'", baseUrl, ex);
            matrixAccount.setState(MatrixAccount.State.NOT_CONNECTED);
        }
    }

    private OkHttpClient buildConfiguredHttpClient(NetworkConnectionConfig networkConnectionConfig) {
        int timeout = networkConnectionConfig.getConnectionTimeout();
        return new OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    private void login() {
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin(matrixAccount.getAccountConfig().getLogin());
        loginPasswordDto.setPassword(matrixAccount.getAccountConfig().getPassword());
        Response<LoginAnswerDto> loginResponse;
        try {
            loginResponse = matrixService.login(loginPasswordDto).execute();
        } catch (IOException ex) {
            LOG.error("Failed to login with {}", matrixAccount.getAccountConfig(), ex);
            matrixAccount.addMessage("Failed to login, because of input/output error");
            return;
        }
        if (loginResponse.isSuccessful()) {
            LoginAnswerDto answerDto = loginResponse.body();
            matrixAccount.setAccessToken(answerDto.getAccessToken());
            matrixAccount.setHomeServer(answerDto.getHomeServer());
            matrixAccount.setUserId(answerDto.getUserId());
            matrixAccount.addMessage("Successfully logged in. Token=" + matrixAccount.getAccessToken());
            matrixAccount.setState(MatrixAccount.State.LOGGEDIN);
        } else {
            ApiError apiError = extractError(loginResponse);
            matrixAccount.addMessage("Failed to log in with " + apiError);
        }
    }

    private ApiError extractError(Response<?> response) {
        int code = response.code();
        ResponseBody errorResponseBody = response.errorBody();
        if (errorResponseBody == null)
            return createUnknownError(code);
        try {
            ErrorDto errorDto = errorConverter.convert(errorResponseBody);
            return new ApiError(code, errorDto.getErrorCode(), errorDto.getErrorMessage());
        } catch (IOException ex) {
            LOG.error("Failed to get error response body", ex);
            return createUnknownError(code);
        }
    }

    private ApiError createUnknownError(int code) {
        return new ApiError(code, "UNKNOWN", "Unknown error");
    }

    private void initialSync() {
       boolean success = sync(() -> matrixService.sync(matrixAccount.getAccessToken()).execute());
       if (success)
           matrixAccount.setState(MatrixAccount.State.INITIAL_SYNCED);
    }

    private void stepSync() {
        sync(() -> matrixService.sync(matrixAccount.getAccessToken(), matrixAccount.getNextBatchId(), 3000).execute());
    }

    private boolean sync(ThrowingSupplier<Response<SyncResponseDto>> supplier) {
        Response<SyncResponseDto> response;
        try {
            response = supplier.get();
        } catch (IOException ex) {
            LOG.error("Failed to synchronize {} with server because of input/output error", matrixAccount.getAccountConfig(), ex);
            matrixAccount.addMessage("Failed to sync because of IOException");
            return false;
        }
        if (response.isSuccessful()) {
            SyncResponseDto syncResponseDto = response.body();
            matrixAccount.setNextBatchId(syncResponseDto.getNextBatch());
            List<String> newMessages = extractNewMessages(syncResponseDto);
            matrixAccount.addMessages(newMessages);
            return true;
        } else {
            ApiError apiError = extractError(response);
            matrixAccount.addMessage("Failed to sync with " + apiError);
            return false;
        }
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
