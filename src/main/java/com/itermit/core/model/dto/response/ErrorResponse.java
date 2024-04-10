package com.itermit.core.model.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ErrorResponse {

    private final String errorMessage;
    private final int errorCode;
}
