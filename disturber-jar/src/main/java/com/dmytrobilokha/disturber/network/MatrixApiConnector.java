package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.ProxyServer;
import com.dmytrobilokha.disturber.network.dto.ErrorDto;
import com.dmytrobilokha.disturber.network.dto.EventContentDto;
import com.dmytrobilokha.disturber.network.dto.JoinResponseDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SendEventResponseDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * The class to create connection and issue network requests to the Matrix Server. Here should be implementations for
 * low-level network related functionality.
 */
class MatrixApiConnector {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixApiConnector.class);

    private MatrixService matrixService;
    private Converter<ResponseBody, ErrorDto> errorConverter;

    void createConnection(String baseUrl, int networkTimeout, ProxyServer proxyServer) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(buildConfiguredHttpClient(networkTimeout, proxyServer))
                .build();
        matrixService = retrofit.create(MatrixService.class);
        errorConverter = retrofit.responseBodyConverter(ErrorDto.class, new Annotation[0]);
    }

    private OkHttpClient buildConfiguredHttpClient(int networkTimeout, ProxyServer proxyServer) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(LOG::debug);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .readTimeout(networkTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(networkTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(networkTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true);
        if (proxyServer != null)
            clientBuilder.proxy(new Proxy(Proxy.Type.HTTP
                    , new InetSocketAddress(proxyServer.getHost(), proxyServer.getPort())));
        return clientBuilder.build();
    }

    LoginAnswerDto login(LoginPasswordDto loginPassword) throws ApiRequestException, ApiConnectException {
        validateConnection();
        return callServer(matrixService.login(loginPassword));
    }

    private void validateConnection() {
        if (matrixService == null) {
            throw new IllegalStateException("API misuse detected. First the connection should be created");
        }
    }

    SyncResponseDto sync(String accessToken) throws ApiRequestException, ApiConnectException {
        validateConnection();
        return callServer(matrixService.sync(accessToken));
    }

    SyncResponseDto sync(String accessToken, String since, int timeout) throws ApiRequestException, ApiConnectException {
        validateConnection();
        return callServer(matrixService.sync(accessToken, since, timeout));
    }

    SendEventResponseDto sendMessageEvent(String accessToken, String roomId, String eventType, String txnId
            , EventContentDto eventContentDto) throws ApiRequestException, ApiConnectException {
        validateConnection();
        return callServer(matrixService.sendMessageEvent(roomId, eventType, txnId, accessToken, eventContentDto));
    }

    JoinResponseDto joinRoom(String accessToken, String roomId) throws ApiRequestException, ApiConnectException {
        validateConnection();
        return callServer(matrixService.joinRoom(roomId, accessToken));
    }

    private <T> T callServer(Call<T> call) throws ApiRequestException, ApiConnectException {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException ex) {
            throw new ApiConnectException("Failed to issue network request because of input/output error", ex);
        }
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new ApiRequestException(extractError(response));
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
            LOG.error("Failed to get error response body from {}", response, ex);
            return createUnknownError(code);
        }
    }

    private ApiError createUnknownError(int code) {
        return new ApiError(code, "UNKNOWN", "Unknown error");
    }

}
