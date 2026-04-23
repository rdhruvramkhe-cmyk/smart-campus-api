package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.storage.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private DataStore dataStore = DataStore.getInstance();

    // get all sensors, optionally filter by type using query param
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(dataStore.getSensors().values());

        // if type param is provided, filter the list
        if (type != null && !type.isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    // create a new sensor - roomId must exist
    @POST
    public Response createSensor(Sensor sensor) {
        // check if the room actually exists before adding the sensor
        if (sensor.getRoomId() == null || !dataStore.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + sensor.getRoomId() + "' does not exist. Cannot register sensor.");
        }

        dataStore.getSensors().put(sensor.getId(), sensor);

        // also add this sensor's id to the room's sensor list
        dataStore.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // create empty readings list for this sensor
        dataStore.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // sub-resource locator - delegates to SensorReadingResource
    // note: no @GET or @POST here, thats the point of a sub-resource locator
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        // make sure the sensor exists first
        if (!dataStore.getSensors().containsKey(sensorId)) {
            throw new NotFoundException("Sensor not found with id: " + sensorId);
        }
        return new SensorReadingResource(sensorId);
    }
}
