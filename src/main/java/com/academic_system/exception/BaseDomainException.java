package com.academic_system.exception;

public abstract class BaseDomainException extends RuntimeException {
    
    private final String resourceName;
    private final String field;

    protected BaseDomainException(String message) {
        super(message);
        this.resourceName = null;
        this.field = null;
    }

    protected BaseDomainException(String message, String resourceName) {
        super(message);
        this.resourceName = resourceName;
        this.field = null;
    }

    protected BaseDomainException(String message, String resourceName, String field) {
        super(message);
        this.resourceName = resourceName;
        this.field = field;
    }

    protected BaseDomainException(String message, Throwable cause) {
        super(message, cause);
        this.resourceName = null;
        this.field = null;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getField() {
        return field;
    }
}
