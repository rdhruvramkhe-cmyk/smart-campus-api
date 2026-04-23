package com.smartcampus.exception;

// thrown when a sensor references a roomId that doesnt exist
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
