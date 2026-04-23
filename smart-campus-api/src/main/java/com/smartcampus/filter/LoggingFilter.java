package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

// logs every request and response that goes through the api
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    // called for every incoming request
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("Request: " + requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri());
    }

    // called for every outgoing response
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("Response status: " + responseContext.getStatus());
    }
}
