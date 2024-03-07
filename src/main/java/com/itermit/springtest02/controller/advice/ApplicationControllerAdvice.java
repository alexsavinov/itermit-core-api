package com.itermit.springtest02.controller.advice;

import com.itermit.springtest02.exception.*;
import com.itermit.springtest02.model.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleArticleNotFound(ArticleNotFoundException exception) {
        log.warn("Cannot find article: {}", exception.getMessage());
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 40503);

        return ResponseEntity.status(NOT_FOUND).body(responseBody);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        log.warn("Cannot find user: {}", exception.getMessage());
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 40403);

        return ResponseEntity.status(NOT_FOUND).body(responseBody);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        log.warn("User already exists: {}", exception.getMessage());
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 40903);

        return ResponseEntity.status(CONFLICT).body(responseBody);
    }

    @ExceptionHandler(UserIncorrectException.class)
    public ResponseEntity<ErrorResponse> handleUserIdIncorrect(UserIncorrectException exception) {
        log.warn("User's id belongs to another user: {}", exception.getMessage());
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 41003);

        return ResponseEntity.status(CONFLICT).body(responseBody);
    }

//    @ExceptionHandler(UserCannotDeleteException.class)
//    public ResponseEntity<ErrorResponse> handleUserCannotDeleteException(UserCannotDeleteException exception) {
//        log.warn("User cannot be deleted {}", exception.getMessage());
//        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 41103);
//
//        return ResponseEntity.status(CONFLICT).body(responseBody);
//    }
//
//    @ExceptionHandler(UsernameAlreadyTakenException.class)
//    public ResponseEntity<ErrorResponse> handleUsernameAlreadyTakenException(UsernameAlreadyTakenException exception) {
//        log.warn("Username is already taken: {}", exception.getMessage());
//        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 41203);
//
//        return ResponseEntity.status(CONFLICT).body(responseBody);
//    }
//
//    @ExceptionHandler(EmailAlreadyInUseException.class)
//    public ResponseEntity<ErrorResponse> handleEmailAlreadyInUseException(EmailAlreadyInUseException exception) {
//        log.warn("Email is already in use: {}", exception.getMessage());
//        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 41303);
//
//        return ResponseEntity.status(CONFLICT).body(responseBody);
//    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(RefreshTokenExpiredException exception, WebRequest request) {
        log.warn("Refresh token expired error: {} {}", exception.getMessage(), request.getDescription(false));
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 10001);

        return ResponseEntity.status(CONFLICT).body(responseBody);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException exception, WebRequest request) {
        log.warn("Refresh token not found error: {} {}", exception.getMessage(), request.getDescription(false));
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 10002);

        return ResponseEntity.status(CONFLICT).body(responseBody);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleNoPermissionsException(UnauthenticatedException exception, WebRequest request) {
        log.warn("Permission denied: {} {}", exception.getMessage(), request.getDescription(false));
        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 10401);

        return ResponseEntity.status(UNAUTHORIZED).body(responseBody);
    }

//    @ExceptionHandler(CustomBadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleBadCredentialsException(CustomBadCredentialsException exception, WebRequest request) {
//        log.warn("Bad credentials: {} {}", exception.getMessage(), request.getDescription(false));
//        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 10402);
//
//        return ResponseEntity.status(UNAUTHORIZED).body(responseBody);
//    }

//    @ExceptionHandler(NoPermissionsException.class)
//    public ResponseEntity<ErrorResponse> handleNoPermissionsException(NoPermissionsException exception, WebRequest request) {
//        log.warn("Permission denied: {} {}", exception.getMessage(), request.getDescription(false));
//        ErrorResponse responseBody = ErrorResponse.of(exception.getMessage(), 10403);
//
//        return ResponseEntity.status(FORBIDDEN).body(responseBody);
//    }
}
