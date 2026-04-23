package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

// root endpoint that gives info about the api
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Smart Campus API");
        info.put("version", "v1");
        info.put("description", "API for managing campus rooms and sensors");
        info.put("contact", "admin@smartcampus.university.ac.uk");

        // links to main resource collections (HATEOAS style)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        info.put("resources", links);

        return Response.ok(info).build();
    }
}
