package com.backend.CodeSheriff.Exception;

import com.backend.CodeSheriff.Model.ApiErrorResponse; // <-- Import the new model
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiIntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleAiIntegrationError(AiIntegrationException ex) {
        log.error("AI Integration failed: ", ex);

        ApiErrorResponse errorBody = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("AI Service Unavailable")
                .message("Failed to communicate with IBM Watsonx. Please try again later.")
                .build();

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericError(Exception ex) {
        log.error("An unexpected error occurred: ", ex);

        ApiErrorResponse errorBody = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred.")
                .build();

        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}