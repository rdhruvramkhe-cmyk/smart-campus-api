package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.storage.DataStore;
import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// this is a sub-resource class, no @Path at class level
// it gets instantiated by the locator method in SensorResource
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;
    private DataStore dataStore = DataStore.getInstance();

    // sensorId is passed from the parent resource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // get all readings for this sensor
    @GET
    public Response getReadings() {
        List<SensorReading> readings = dataStore.getSensorReadings()
                .getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    // add a new reading to this sensor
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensors().get(sensorId);

        // sensor in MAINTENANCE cant accept new readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is currently in MAINTENANCE mode and cannot accept readings.");
        }

        // auto generate id and timestamp if client didnt send them
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // add to readings list
        dataStore.getSensorReadings()
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // important: update the parent sensor's currentValue to match this new reading
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
