package com.academic_system.exception;

public class DuplicateResourceException extends BaseDomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, String resourceName) {
        super(message, resourceName);
    }

    public DuplicateResourceException(String message, String resourceName, String field) {
        super(message, resourceName, field);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
