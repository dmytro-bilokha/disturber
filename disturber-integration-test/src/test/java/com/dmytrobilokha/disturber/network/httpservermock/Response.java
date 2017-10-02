package com.dmytrobilokha.disturber.network.httpservermock;

public class Response {

    private final int statusCode;
    private final String resource;

    public Response(int statusCode, String resource) {
        this.statusCode = statusCode;
        this.resource = resource;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResource() {
        return resource;
    }

}
