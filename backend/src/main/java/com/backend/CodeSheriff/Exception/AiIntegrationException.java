package com.backend.CodeSheriff.Exception;

public class AiIntegrationException extends RuntimeException {
    public AiIntegrationException(String message, Throwable cause){
        super(message, cause);
    }
}
