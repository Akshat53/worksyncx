package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create or update a department with a duplicate department code.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDepartmentCodeException extends RuntimeException {

    public DuplicateDepartmentCodeException(String message) {
        super(message);
    }

    public DuplicateDepartmentCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
