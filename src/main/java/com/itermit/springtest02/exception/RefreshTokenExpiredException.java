package com.itermit.springtest02.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String token, String message) {
        super(message);
    }
}
