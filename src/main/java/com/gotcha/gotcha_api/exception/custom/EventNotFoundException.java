package com.gotcha.gotcha_api.exception.custom;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
