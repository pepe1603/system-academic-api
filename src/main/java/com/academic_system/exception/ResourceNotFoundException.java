package com.academic_system.exception;

public class ResourceNotFoundException extends BaseDomainException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, String resourceName) {
        super(message, resourceName);
    }

    public ResourceNotFoundException(String message, String resourceName, String field) {
        super(message, resourceName, field);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
