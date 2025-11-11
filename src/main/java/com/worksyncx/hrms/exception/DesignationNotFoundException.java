package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested designation is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DesignationNotFoundException extends RuntimeException {

    public DesignationNotFoundException(String message) {
        super(message);
    }

    public DesignationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
