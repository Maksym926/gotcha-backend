package com.gotcha.gotcha_api.exception.custom;

public class StaticContentNotFoundException extends RuntimeException {
    public StaticContentNotFoundException(String message) {
        super(message);
    }
}
