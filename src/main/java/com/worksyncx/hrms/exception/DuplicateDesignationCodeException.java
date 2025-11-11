package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create or update a designation with a duplicate designation code.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDesignationCodeException extends RuntimeException {

    public DuplicateDesignationCodeException(String message) {
        super(message);
    }

    public DuplicateDesignationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
