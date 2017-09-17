package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.network.dto.ErrorDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

/**
 * The class to create connection and issue network requests to the Matrix Server. Here should be implementations for
 * low-level network related functionality.
 */
class MatrixApiConnector {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixApiConnector.class);

    private MatrixService matrixService;
    private Converter<ResponseBody, ErrorDto> errorConverter;

    void createConnection(String baseUrl, int networkTimeout) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(buildConfiguredHttpClient(networkTimeout))
                .build();
        matrixService = retrofit.create(MatrixService.class);
        errorConverter = retrofit.responseBodyConverter(ErrorDto.class, new Annotation[0]);
    }

    private OkHttpClient buildConfiguredHttpClient(int networkTimeout) {
        return new OkHttpClient.Builder()
                .readTimeout(networkTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(networkTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(networkTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
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
