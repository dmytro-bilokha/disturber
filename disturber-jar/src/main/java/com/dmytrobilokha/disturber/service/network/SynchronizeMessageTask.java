package com.dmytrobilokha.disturber.service.network;

import com.dmytrobilokha.disturber.model.network.MessageDto;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageTask extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizeMessageTask.class);

    private final List<String> messageList;
    private final String baseUrl;

    SynchronizeMessageTask(List<String> messageList, String baseUrl) {
        this.messageList = messageList;
        this.baseUrl = baseUrl;
    }

    @Override
    protected Void call() throws Exception {
        MatrixService matrixService = null;
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
            matrixService = retrofit.create(MatrixService.class);
        } catch (Exception ex) {
            LOG.error("Failed to open connection to the endpoint '{}'", baseUrl, ex);
            return null;
        }
        while (!isCancelled()) {
            Thread.sleep(5000);
            Response<MessageDto> messageDtoResponse = matrixService.getQuote().execute();
            MessageDto messageDto = messageDtoResponse.body();
            Platform.runLater(() -> messageList.add(messageDto.getValue().getQuote()));
        }
        return null;
    }

}
