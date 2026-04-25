package com.academic_system.exception;

public class EmailSendException extends RuntimeException {
    
    private final String recipient;

    public EmailSendException(String message, String recipient) {
        super(message);
        this.recipient = recipient;
    }

    public EmailSendException(String message, String recipient, Throwable cause) {
        super(message, cause);
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }
}