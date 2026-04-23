package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    // base url for the api
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        // scans the package to find all resource classes, filters, mappers etc
        final ResourceConfig config = new ResourceConfig().packages("com.smartcampus");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws Exception {
        final HttpServer server = startServer();
        System.out.println("Smart Campus API started at " + BASE_URI);
        System.out.println("Hit Ctrl+C to stop the server...");

        // keep the server running
        Thread.currentThread().join();
    }
}
