package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create or update a shift with a duplicate shift code.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateShiftCodeException extends RuntimeException {

    public DuplicateShiftCodeException(String message) {
        super(message);
    }

    public DuplicateShiftCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
