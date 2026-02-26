package com.gotcha.gotcha_api.exception;

import com.gotcha.gotcha_api.exception.custom.EventNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.RSVPEventNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //DTO validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                errors
        );
    }

    // EventNotFoundException
    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEventNotFoundException(EventNotFoundException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return  new ErrorResponse(
                LocalDateTime.now(),
                404,
                errors
        );
    }
    // ImageFileNotFoundException
    @ExceptionHandler(ImageFileNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleFileUploadException(
            ImageFileNotFoundException ex){

        Map<String, String> errors = new HashMap<>();

        errors.put("message", ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now(),
                500,
                errors
        );
    }
    // RSVPEventNotFoundException
    @ExceptionHandler(RSVPEventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEventRSVPNotFoundException(RSVPEventNotFoundException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return  new ErrorResponse(
                LocalDateTime.now(),
                404,
                errors
        );
    }
}
