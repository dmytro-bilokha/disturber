package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.connection.NetworkConnectionConfig;
import com.dmytrobilokha.disturber.network.dto.ErrorDto;
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
import java.util.function.Function;

/**
 * The class to create connection and issue network requests to the Matrix Server. Here should be implementations for
 * low-level network related functionality.
 */
class MatrixApiConnector {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixApiConnector.class);

    private final NetworkConnectionConfig networkConnectionConfig;

    private MatrixService matrixService;
    private Converter<ResponseBody, ErrorDto> errorConverter;

    MatrixApiConnector(NetworkConnectionConfig networkConnectionConfig) {
        this.networkConnectionConfig = networkConnectionConfig;
    }

    void createConnection(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(buildConfiguredHttpClient())
                .build();
        matrixService = retrofit.create(MatrixService.class);
        errorConverter = retrofit.responseBodyConverter(ApiError.class, new Annotation[0]);
    }

    private OkHttpClient buildConfiguredHttpClient() {
        int timeout = networkConnectionConfig.getConnectionTimeout();
        return new OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    <T> T issueRequest(Function<MatrixService, Call<T>> requestFunction) throws ApiRequestException, ApiConnectException {
        Call<T> call = requestFunction.apply(matrixService);
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
