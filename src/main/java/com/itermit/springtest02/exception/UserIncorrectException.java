package com.itermit.springtest02.exception;

public class UserIncorrectException extends RuntimeException {

    public UserIncorrectException(String message) {
        super(message);
    }
}
