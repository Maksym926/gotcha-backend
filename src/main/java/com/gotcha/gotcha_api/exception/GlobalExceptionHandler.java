package com.gotcha.gotcha_api.exception;

import com.gotcha.gotcha_api.exception.custom.*;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
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

    // Stripe exception
    @ExceptionHandler(StripeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleStripeException(StripeException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                500,
                errors
        );
    }

    // Invalid password reset token
    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidPasswordResetTokenException(InvalidPasswordResetTokenException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                errors
        );
    }

    // Invalid refresh token
    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidRefreshTokenException(InvalidRefreshTokenException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                401,
                errors
        );
    }

    // Authentication failures (bad credentials, disabled account, etc.) from controller-level auth calls
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Invalid credentials");
        return new ErrorResponse(
                LocalDateTime.now(),
                401,
                errors
        );
    }

    // Fallback for unexpected runtime errors
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                500,
                errors
        );
    }
}
