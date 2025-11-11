package com.worksyncx.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a subscription limit is exceeded.
 * This includes employee count limits, storage limits, etc.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class SubscriptionLimitException extends RuntimeException {

    public SubscriptionLimitException(String message) {
        super(message);
    }

    public SubscriptionLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
