package com.dmytrobilokha.disturber.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a simple HTTP server implementation used for REST client testing
 */
public class HttpServerMock {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerMock.class);

    private final HttpServer server;
    private final Map<URI, String> requestUriToOutputResourceMap = new ConcurrentHashMap<>();

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

    public HttpServerMock addUriMock(URI requestUri, String mockResource) {
        requestUriToOutputResourceMap.put(requestUri, mockResource);
        return this;
    }

    private class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI requestUri = httpExchange.getRequestURI();
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

    }

}
