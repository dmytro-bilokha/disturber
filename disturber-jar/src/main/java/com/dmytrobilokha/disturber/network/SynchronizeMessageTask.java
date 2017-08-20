package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.model.network.MessageDto;
import com.dmytrobilokha.disturber.config.connection.NetworkConnectionConfig;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageTask extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizeMessageTask.class);

    private final List<String> messageList;
    private final String baseUrl;
    private final NetworkConnectionConfig connectionConfig;

    SynchronizeMessageTask(List<String> messageList, String baseUrl, NetworkConnectionConfig connectionConfig) {
        this.messageList = messageList;
        this.baseUrl = baseUrl;
        this.connectionConfig = connectionConfig;
    }

    @Override
    protected Void call() throws Exception {
        MatrixService matrixService = null;
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .client(getConfiguredHttpClient())
                    .build();
            matrixService = retrofit.create(MatrixService.class);
        } catch (Exception ex) {
            LOG.error("Failed to open connection to the endpoint '{}'", baseUrl, ex);
            return null;
        }
        while (!isCancelled()) {
            Thread.sleep(connectionConfig.getConnectionInterval());
            Response<MessageDto> messageDtoResponse = matrixService.getQuote().execute();
            MessageDto messageDto = messageDtoResponse.body();
            Platform.runLater(() -> messageList.add(messageDto.getValue().getQuote()));
        }
        return null;
    }

    private OkHttpClient getConfiguredHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(connectionConfig.getConnectionTimeout(), TimeUnit.SECONDS)
                .connectTimeout(connectionConfig.getConnectionTimeout(), TimeUnit.SECONDS)
                .writeTimeout(connectionConfig.getConnectionTimeout(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

}
