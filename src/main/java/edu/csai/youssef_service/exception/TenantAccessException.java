package edu.csai.youssef_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a request attempts to access data belonging to a different tenant.
 * This is the defence-in-depth layer; the Hibernate @Filter is the first line.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TenantAccessException extends RuntimeException {
    public TenantAccessException(String message) {
        super(message);
    }
}
