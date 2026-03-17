package com.gotcha.gotcha_api.exception.custom;

import java.io.IOException;

public class ImageFileNotFoundException extends RuntimeException {
    public ImageFileNotFoundException(String message, IOException ex) {
        super(message);
    }
}
