package com.pupuputeam.backend.controller;

import com.pupuputeam.backend.dto.response.ErrorResponse;
import com.pupuputeam.backend.exception.AuthException;
import com.pupuputeam.backend.exception.InvalidLocationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parameter mismatch: {}", ex.getName());
        ErrorResponse response = new ErrorResponse("Invalid Parameter Type",
                "Incorrect format to '" + ex.getName() + "'. Expected type: " + ex.getRequiredType().getSimpleName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.error("Binding error: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("Data binding error",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .findFirst()
                        .orElse("Data binding error"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthErrors(AuthException ex) {
        log.warn("Auth attempt failed: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("Unauthorized", "Wrong email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidLocationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLocationException(InvalidLocationException ex) {
        log.warn("Invalid location: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("Invalid location", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("General error: {}", ex);
        ErrorResponse response = new ErrorResponse("Internal Server Error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}