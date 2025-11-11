package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to delete the last remaining tenant admin.
 * At least one tenant admin must exist to ensure tenant accessibility and management.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class LastTenantAdminException extends RuntimeException {

    public LastTenantAdminException(String message) {
        super(message);
    }

    public LastTenantAdminException(String message, Throwable cause) {
        super(message, cause);
    }
}
