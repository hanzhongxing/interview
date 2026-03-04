package com.interview.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class ConfigurationMissingException extends RuntimeException {
    public ConfigurationMissingException(String message) {
        super(message);
    }
}
