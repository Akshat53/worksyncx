package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * General exception for employee validation errors (phone number, etc.).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmployeeValidationException extends RuntimeException {

    public EmployeeValidationException(String message) {
        super(message);
    }

    public EmployeeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
