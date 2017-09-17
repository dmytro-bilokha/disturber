package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MatrixApiConnectorTest {

    private static final String BASE_URL = "http://localhost:64444/";
    private static final String SYNC_PATH = "_matrix/client/r0/sync";
    private static final String LOGIN_PATH = "_matrix/client/r0/login";
    private static final String JSONSET_BASE = "/jsonset/";
    private static final int NETWORK_TIMEOUT = 200;

    private static HttpServerMock httpServerMock;

    private MatrixApiConnector apiConnector;

    @BeforeClass
    public static void setupServer() throws Exception {
        httpServerMock = new HttpServerMock("/", 64444);
        httpServerMock.start();
    }

    @AfterClass
    public static void shutdownServer() {
        httpServerMock.stop();
    }

    @Before
    public void reset() {
        apiConnector = new MatrixApiConnector();
        httpServerMock.reset();
    }

    @Test
    public void testLogsIn() throws Exception {
        URI loginUri = new URI("/" + LOGIN_PATH);
        httpServerMock.setUriMock(loginUri, JSONSET_BASE + "login.json");
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin("MY_LOGIN");
        loginPasswordDto.setPassword("MY_PASSWORD");
        apiConnector.createConnection(BASE_URL, NETWORK_TIMEOUT);
        LoginAnswerDto loginAnswerDto = apiConnector.login(loginPasswordDto);
        HttpServerMock.RequestCapture requestCapture = httpServerMock.getRequestCapture();
        assertNotNull(requestCapture);
        assertEquals("POST", requestCapture.getMethod());
        assertEquals(loginUri, requestCapture.getUri());
        String requestBody = requestCapture.getBody();
        assertNotNull(requestBody);
        assertTrue(isJsonFieldPresent(requestBody, "user", "MY_LOGIN"));
        assertTrue(isJsonFieldPresent(requestBody, "password", "MY_PASSWORD"));
        assertTrue(isJsonFieldPresent(requestBody, "type", "m.login.password"));
        assertNotNull(loginAnswerDto);
        assertEquals("SECURE_TOKEN", loginAnswerDto.getAccessToken());
    }

    private boolean isJsonFieldPresent(String jsonString, String fieldName, String fieldValue) {
        String pattern = ".*\"" + fieldName + "\" *: *\"" + fieldValue + "\".*";
        return Pattern.matches(pattern, jsonString);
    }

}
