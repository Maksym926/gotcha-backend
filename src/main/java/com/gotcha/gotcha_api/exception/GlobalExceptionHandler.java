package com.gotcha.gotcha_api.exception;

import com.gotcha.gotcha_api.exception.custom.*;
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

    // ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
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
    // DuplicateEmailException
    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateEmailException(DuplicateEmailException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                409,
                errors
        );
    }
    // image generation exception
    @ExceptionHandler(ImageGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleImageGenerationException(ImageGenerationException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                500,
                errors
        );
    }

    //out-of-stock exception
    @ExceptionHandler(ProductOutOfStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOutOfStockException(ProductOutOfStockException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                errors
        );
    }

    // insufficient coins exception
    @ExceptionHandler(InsufficientCoinsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientCoinsException(InsufficientCoinsException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                errors
        );
    }
}
