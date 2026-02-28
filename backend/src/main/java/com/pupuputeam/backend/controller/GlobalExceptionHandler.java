package com.pupuputeam.backend.controller;

import com.pupuputeam.backend.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid Parameter Type");
        response.put("message", "Incorrect format to '" + ex.getName() + "'. Expected type: " + ex.getRequiredType().getSimpleName());

        // Маленька підказка для дат
        if (ex.getRequiredType() == Instant.class) {
            response.put("hint", "Date format must be ISO-8601, for instance: 2026-02-28T15:30:00Z");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleBindException(BindException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Data Binding Error");
        response.put("message", "Data binding error. Check the parameters you sent.");

        if (ex.getFieldError() != null) {
            response.put("failed_field", ex.getFieldError().getField());
            response.put("reason", ex.getFieldError().getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthErrors(AuthException ex) {
        System.out.println("Auth Error: " + ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", "Wrong email or password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}