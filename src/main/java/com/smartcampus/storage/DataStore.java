package com.smartcampus.storage;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// singleton class to store all our data in memory
// using hashmaps instead of a database as required
public class DataStore {

    private static DataStore instance;

    private Map<String, Room> rooms = new HashMap<>();
    private Map<String, Sensor> sensors = new HashMap<>();
    // key is sensorId, value is list of readings for that sensor
    private Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    private DataStore() {
        // private constructor so no one can create another instance
    }

    // synchronized so its thread safe
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getSensorReadings() { return sensorReadings; }
}
