package com.walletservice.exception;


import lombok.Getter;

@Getter
public class EventSerializationException extends RuntimeException {

    private final ErrorCode errorCode;

    public EventSerializationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
