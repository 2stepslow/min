package com.green.min.exceptions;

public class AuthorizationFailureException extends RuntimeException {
    public AuthorizationFailureException(String message) {
        super(message);
    }
}
