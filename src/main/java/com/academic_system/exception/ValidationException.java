package com.academic_system.exception;

public class ValidationException extends BaseDomainException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String resourceName) {
        super(message, resourceName);
    }

    public ValidationException(String message, String resourceName, String field) {
        super(message, resourceName, field);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
