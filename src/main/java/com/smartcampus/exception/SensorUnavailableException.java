package com.smartcampus.exception;

// thrown when trying to post a reading to a sensor thats in MAINTENANCE
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
