package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.network.dto.EventContentDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SendEventResponseDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import com.dmytrobilokha.disturber.network.httpservermock.HttpServerMock;
import com.dmytrobilokha.disturber.network.httpservermock.RequestCapture;
import com.dmytrobilokha.disturber.network.httpservermock.Response;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MatrixApiConnectorTest {

    private static String baseUrl;
    private static final String SYNC_PATH = "_matrix/client/r0/sync";
    private static final String LOGIN_PATH = "_matrix/client/r0/login";
    private static final String JSONSET_BASE = "/jsonset/";
    private static final int NETWORK_TIMEOUT = 200;

    private static HttpServerMock httpServerMock;

    private MatrixApiConnector apiConnector;

    @BeforeClass
    public static void setupServer() {
        httpServerMock = HttpServerMock.runOnRandomHiPort();
        baseUrl = httpServerMock.getBaseUrl();
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
        httpServerMock.setUriMock(loginUri, new Response(200, JSONSET_BASE + "login.json"));
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin("MY_LOGIN");
        loginPasswordDto.setPassword("MY_PASSWORD");
        apiConnector.createConnection(baseUrl, NETWORK_TIMEOUT, null);
        LoginAnswerDto loginAnswerDto = apiConnector.login(loginPasswordDto);
        RequestCapture requestCapture = getFirstAndOnlyRequestCapture();
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

    private RequestCapture getFirstAndOnlyRequestCapture() {
        RequestCapture[] requestCaptures = httpServerMock.getRequestCaptureHistory();
        assertEquals(1, requestCaptures.length);
        return requestCaptures[0];
    }

    @Test
    public void testHandlesLoginFail() throws URISyntaxException {
        URI loginUri = new URI("/" + LOGIN_PATH);
        httpServerMock.setUriMock(loginUri, new Response(403, JSONSET_BASE + "login403.json"));
        LoginPasswordDto loginPasswordDto = new LoginPasswordDto();
        loginPasswordDto.setLogin("MY_LOGIN");
        loginPasswordDto.setPassword("MY_PASSWORD");
        apiConnector.createConnection(baseUrl, NETWORK_TIMEOUT, null);
        try {
            LoginAnswerDto loginAnswerDto = apiConnector.login(loginPasswordDto);
        } catch (ApiRequestException ex) {
            ApiError apiError = ex.getApiError();
            assertNotNull(apiError);
            assertEquals(403, apiError.getNetworkCode());
            assertEquals("M_FORBIDDEN", apiError.getErrorCode());
            assertNull(apiError.getErrorMessage());
        } catch (ApiConnectException ex) {
            fail("On failed login with 403 response code, connector should throw ApiRequesException");
        }

    }

    private boolean isJsonFieldPresent(String jsonString, String fieldName, String fieldValue) {
        String pattern = ".*\"" + fieldName + "\" *: *\"" + fieldValue + "\".*";
        return Pattern.matches(pattern, jsonString);
    }

    @Test
    public void testSynchronizesInGeneral() throws URISyntaxException, ApiConnectException, ApiRequestException {
        URI syncUri = new URI("/" + SYNC_PATH + "?access_token=ACCESS_TOKEN");
        httpServerMock.setUriMock(syncUri, new Response(200, JSONSET_BASE + "sync.json"));
        apiConnector.createConnection(baseUrl, NETWORK_TIMEOUT, null);
        SyncResponseDto syncResponseDto = apiConnector.sync("ACCESS_TOKEN");
        RequestCapture requestCapture = getFirstAndOnlyRequestCapture();
        assertEquals("GET", requestCapture.getMethod());
        assertEquals(syncUri, requestCapture.getUri());
        assertNotNull(syncResponseDto);
        assertEquals("s72595_4483_1934", syncResponseDto.getNextBatch());
    }

    @Test
    public void testSendsTextMessage() throws URISyntaxException, ApiConnectException, ApiRequestException {
        URI sendMessageUri = new URI("/_matrix/client/r0/rooms/!636q39766251:example.com/send/m.room.message/1?access_token=ACCESS_TOKEN");
        httpServerMock.setUriMock(sendMessageUri, new Response(200, JSONSET_BASE + "sendMessage.json"));
        apiConnector.createConnection(baseUrl, NETWORK_TIMEOUT, null);
        EventContentDto contentDto = new EventContentDto();
        contentDto.setBody("Hello World");
        contentDto.setMsgType("msg.text");
        SendEventResponseDto sendEventResponseDto = apiConnector.sendMessageEvent(
                "ACCESS_TOKEN"
                , "!636q39766251:example.com"
                , "m.room.message"
                , "1"
                , contentDto);
        RequestCapture requestCapture = getFirstAndOnlyRequestCapture();
        assertEquals("PUT", requestCapture.getMethod());
        assertEquals(sendMessageUri, requestCapture.getUri());
        String requestJson = requestCapture.getBody();
        assertTrue(isJsonFieldPresent(requestJson, "body", "Hello World"));
        assertTrue(isJsonFieldPresent(requestJson, "msgtype", "msg.text"));
        assertNotNull(sendEventResponseDto);
        assertEquals("EVENT_ID", sendEventResponseDto.getEventId());
    }

}
