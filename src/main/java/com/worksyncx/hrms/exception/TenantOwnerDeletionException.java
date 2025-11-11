package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to delete the primary owner of a tenant.
 * The primary owner is the user who created/purchased the tenant subscription and cannot be deleted
 * to prevent orphaned tenants.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TenantOwnerDeletionException extends RuntimeException {

    public TenantOwnerDeletionException(String message) {
        super(message);
    }

    public TenantOwnerDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
