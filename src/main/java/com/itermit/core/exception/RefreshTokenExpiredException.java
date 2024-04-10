package com.itermit.core.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String token, String message) {
        super(message);
    }
}
