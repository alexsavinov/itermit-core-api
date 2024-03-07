package com.itermit.springtest02.exception;

public class NoPermissionsException extends RuntimeException {

    public NoPermissionsException(String message) {
        super(message);
    }
}
