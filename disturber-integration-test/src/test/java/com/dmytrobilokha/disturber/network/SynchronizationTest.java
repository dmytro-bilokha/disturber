package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import okhttp3.OkHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * The class to test synchronization with a matrix server
 */
@Ignore
public class SynchronizationTest {

    //TODO: implement randomizing port number + few retries
    private static final String baseUrl = "http://localhost:64444/";
    private static final String endpointPath = "_matrix/client/r0/sync";

    private static HttpServerMock httpServerMock;

    @BeforeClass
    public static void setupServer() throws Exception {
        httpServerMock = new HttpServerMock("/", 64444);
        httpServerMock.addUriMock(new URI("/" + endpointPath), "/jsonset/sync.json");
        httpServerMock.start();
    }

    @AfterClass
    public static void shutdownServer() {
        httpServerMock.stop();
    }

    @Ignore
    @Test
    public void testReadsData() throws Exception {
        URLConnection connection = new URL(baseUrl + endpointPath).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        reader.lines().forEach(line -> System.out.println(line));
    }

    @Ignore
    @Test
    public void testDeserializesJsonInput() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(getConfiguredHttpClient())
                .build();
        MatrixService matrixService = retrofit.create(MatrixService.class);
        //Response<SyncResponseDto> response = matrixService.sync().execute();
        //System.out.println(response.body());
    }

    //TODO: integrate this stuff into main code and change this test
    @Test
    public void testLogsIn() throws IOException {
        LoginPasswordDto loginPassword = new LoginPasswordDto();
        loginPassword.setLogin("login");
        loginPassword.setPassword("pass");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://matrix.org/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(getConfiguredHttpClient())
                .build();
        MatrixService matrixService = retrofit.create(MatrixService.class);
        Response<LoginAnswerDto> response = matrixService.login(loginPassword).execute();
        System.out.println(response.body());
    }

    private OkHttpClient getConfiguredHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
    }

}
