package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create or update an employee with a duplicate employee code.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmployeeCodeException extends RuntimeException {

    public DuplicateEmployeeCodeException(String message) {
        super(message);
    }

    public DuplicateEmployeeCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
