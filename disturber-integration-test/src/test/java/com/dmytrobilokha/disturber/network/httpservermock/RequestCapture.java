package com.dmytrobilokha.disturber.network.httpservermock;

import java.net.URI;

public class RequestCapture {
    private final URI uri;
    private final String method;
    private final String body;
    private final long nanoTime;

    public RequestCapture(URI uri, String method, String body, long nanoTime) {
        this.uri = uri;
        this.method = method;
        this.body = body;
        this.nanoTime = nanoTime;
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

    public long getNanoTime() {
        return nanoTime;
    }
}
