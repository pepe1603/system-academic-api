package com.academic_system.exception;

public class BusinessRuleException extends BaseDomainException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, String resourceName) {
        super(message, resourceName);
    }

    public BusinessRuleException(String message, String resourceName, String field) {
        super(message, resourceName, field);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
