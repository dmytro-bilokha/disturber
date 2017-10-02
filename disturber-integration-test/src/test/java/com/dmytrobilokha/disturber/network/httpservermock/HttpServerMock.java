package com.dmytrobilokha.disturber.network.httpservermock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * This is a simple HTTP server implementation used for REST client testing
 */
public class HttpServerMock {

    private static final int MAX_TRY = 5;
    private static final int PORT_FROM = 30000;
    private static final int PORT_TO = 65500;
    private static final String LOCALHOST = "http://localhost:";
    private static final RequestCapture[] EMPTY_REQUEST_CAPTURE_ARRAY = new RequestCapture[0];

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerMock.class);

    private final HttpServer server;
    private final String baseUrl;
    private final Map<URI, Response> requestUriToOutputResourceMap = new ConcurrentHashMap<>();
    private final List<RequestCapture> requestCaptureHistory = new CopyOnWriteArrayList<>();

    private HttpServerMock(String context, int port) throws IOException {
        LOG.info("Creating mock HTTP server on port {}...", port);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(context, new Handler());
        baseUrl = LOCALHOST + port + context;
    }

    public static HttpServerMock runOnRandomHiPort() {
        Random portRandom = new Random(System.nanoTime());
        int tryNumber = 1;
        int[] portsTried = new int[MAX_TRY];
        HttpServerMock httpServerMock = null;
        while (httpServerMock == null && tryNumber <= MAX_TRY) {
            int portNumSuggested = portRandom.nextInt(PORT_TO - PORT_FROM) + PORT_FROM;
            portsTried[tryNumber - 1] = portNumSuggested;
            try {
                httpServerMock = new HttpServerMock("/", portNumSuggested);
            } catch (IOException ex) {
                tryNumber++;
            }
        }
        if (httpServerMock == null)
            throw new IllegalStateException("Tried " + tryNumber
                    + " times, but failed to start mock http server. Ports tried: " + Arrays.toString(portsTried));
        return httpServerMock;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(1);
    }

    public HttpServerMock setUriMock(URI requestUri, Response mockResponse) {
        requestUriToOutputResourceMap.put(requestUri, mockResponse);
        return this;
    }

    public HttpServerMock reset() {
        requestUriToOutputResourceMap.clear();
        requestCaptureHistory.clear();
        return this;
    }

    public RequestCapture[] getRequestCaptureHistory() {
        return requestCaptureHistory.toArray(EMPTY_REQUEST_CAPTURE_ARRAY);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI requestUri = httpExchange.getRequestURI();
            requestCaptureHistory.add(new RequestCapture(
                    requestUri
                    , httpExchange.getRequestMethod()
                    , extractRequestBody(httpExchange)
                    , System.nanoTime()));
            Response mockResponse = requestUriToOutputResourceMap.get(requestUri);
            if (mockResponse == null) {
                LOG.error("Requested URI {} has no corresponding mock json set", requestUri);
                throw new IllegalStateException("Requested URI " + requestUri + " has no corresponding mock json set");
            }
            URI resourceUri;
            try {
                resourceUri = getClass().getResource(mockResponse.getResource()).toURI();
            } catch (URISyntaxException e) {
                LOG.error("Unable to get classpath resource {}", mockResponse.getResource());
                throw new IllegalStateException("Unable to get resource '" + mockResponse.getResource() + '\'');
            }
            Path resourcePath = Paths.get(resourceUri);
            httpExchange.sendResponseHeaders(mockResponse.getStatusCode(), Files.size(resourcePath));
            OutputStream os = httpExchange.getResponseBody();
            Files.copy(resourcePath, os);
            os.close();
        }

        private String extractRequestBody(HttpExchange httpExchange) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))) {
                 return reader.lines().collect(Collectors.joining());
            } catch (IOException ex) {
                LOG.error("Failed to read request body", ex);
                throw new IllegalStateException("Unable to read request body", ex);
            }
        }
    }

}
