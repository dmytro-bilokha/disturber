package com.dmytrobilokha.disturber.network.httpservermock;

import java.net.URI;

public class RequestCapture {
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
