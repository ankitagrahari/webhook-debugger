package org.backendbrilliance.uiservice.exception;

public class TierLimitException extends RuntimeException{

    public TierLimitException(String message) {
        super(message);
    }
}
