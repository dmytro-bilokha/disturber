package com.dmytrobilokha.disturber.network;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is a simple HTTP server implementation used for REST client testing
 */
public class HttpServerMock {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerMock.class);

    private final HttpServer server;
    private final Map<URI, String> requestUriToOutputResourceMap = new ConcurrentHashMap<>();
    private volatile RequestCapture requestCapture;

    public HttpServerMock(String context, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(context, new Handler());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(1);
    }

    public HttpServerMock setUriMock(URI requestUri, String mockResource) {
        requestUriToOutputResourceMap.put(requestUri, mockResource);
        return this;
    }

    public HttpServerMock reset() {
        requestUriToOutputResourceMap.clear();
        requestCapture = null;
        return this;
    }

    public RequestCapture getRequestCapture() {
        return requestCapture;
    }

    private class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI requestUri = httpExchange.getRequestURI();
            requestCapture = new RequestCapture(requestUri
                    , httpExchange.getRequestMethod(), extractRequestBody(httpExchange));
            String resource = requestUriToOutputResourceMap.get(requestUri);
            if (resource == null) {
                LOG.error("Requested URI {} has no corresponding mock json set", requestUri);
                throw new IllegalStateException("Requested URI " + requestUri + " has no corresponding mock json set");
            }
            URI resourceUri = null;
            try {
                resourceUri = getClass().getResource(resource).toURI();
            } catch (URISyntaxException e) {
                LOG.error("Unable to get classpath resource {}", resource);
                throw new IllegalStateException("Unable to get resource '" + resource + '\'');
            }
            Path resourcePath = Paths.get(resourceUri);
            httpExchange.sendResponseHeaders(200, Files.size(resourcePath));
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

    public static class RequestCapture {
        private final URI uri;
        private final String method;
        private final String body;

        public RequestCapture(URI uri, String method, String body) {
            this.uri = uri;
            this.method = method;
            this.body = body;
        }

        public URI getUri() {
            return uri;
        }

        public String getMethod() {
            return method;
        }

        public String getBody() {
            return body;
        }
    }
}
