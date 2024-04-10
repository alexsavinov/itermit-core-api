package com.itermit.core.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException(String token, String message) {
        super(message);
    }
}
