package com.uchat.backend.chat.service;

public class LlmServiceException extends RuntimeException {

    private final String code;

    public LlmServiceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public LlmServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
