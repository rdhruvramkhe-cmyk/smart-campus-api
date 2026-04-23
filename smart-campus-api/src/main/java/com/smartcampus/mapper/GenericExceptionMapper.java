package com.smartcampus.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

// catch-all mapper for any unhandled exceptions
// returns 500 without leaking stack trace info to the client
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        // log the actual error on server side for debugging
        LOGGER.severe("Unhandled exception: " + e.getClass().getName() + " - " + e.getMessage());

        // dont expose internal details to the client
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please try again later.");
        error.put("status", 500);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
