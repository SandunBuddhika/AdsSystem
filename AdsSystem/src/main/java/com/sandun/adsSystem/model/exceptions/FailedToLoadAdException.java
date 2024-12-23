package com.sandun.adsSystem.model.exceptions;

public class FailedToLoadAdException extends RuntimeException {
    public FailedToLoadAdException(String message) {
        super(message);
    }
}
