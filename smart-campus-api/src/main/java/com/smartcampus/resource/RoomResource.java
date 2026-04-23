package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.storage.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private DataStore dataStore = DataStore.getInstance();

    // get all rooms
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(dataStore.getRooms().values())).build();
    }

    // create a new room
    @POST
    public Response createRoom(Room room) {
        dataStore.getRooms().put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // get a specific room by id
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found with id: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    // delete a room - but only if it has no sensors assigned
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found with id: " + roomId))
                    .build();
        }

        // cant delete if there are still sensors in the room
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room " + roomId
                    + " because it still has " + room.getSensorIds().size() + " sensor(s) assigned to it.");
        }

        dataStore.getRooms().remove(roomId);
        return Response.noContent().build(); // 204 no content on successful delete
    }
}
