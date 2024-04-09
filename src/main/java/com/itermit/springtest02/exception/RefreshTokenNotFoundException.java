package com.itermit.springtest02.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException(String token, String message) {
        super(message);
    }
}
