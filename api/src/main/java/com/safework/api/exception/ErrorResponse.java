package com.safework.api.exception;

import java.time.LocalDateTime;

/**
 * A standard structure for API error responses.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}