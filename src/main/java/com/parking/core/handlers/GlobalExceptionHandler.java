package com.parking.core.handlers;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.exception.StripeException;

/**
 * Centralized exception handler for the entire application.
 * <p>
 * Catches exceptions thrown by controllers and services and transforms them into
 * structured JSON error responses with consistent format:
 * <pre>
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "..."
 * }
 * </pre>
 * </p>
 *
 * @see ResponseStatusException
 * @see MethodArgumentNotValidException
 * @see StripeException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ResponseStatusException} thrown by services/controllers.
     *
     * @param ex the exception containing HTTP status and reason
     * @return a structured error response with the appropriate status code
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("ResponseStatusException: {} - {}", ex.getStatusCode(), ex.getReason());
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    /**
     * Handles bean validation errors from {@code @Valid} annotations.
     * <p>
     * Returns a map of field names to their respective validation error messages.
     * </p>
     *
     * @param ex the validation exception containing field errors
     * @return {@code 400 BAD_REQUEST} with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles Stripe payment processing errors.
     *
     * @param ex the Stripe exception with status code and message
     * @return a structured error response with the Stripe error status
     */
    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Map<String, Object>> handleStripe(StripeException ex) {
        log.error("Stripe error: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        return buildResponse(status, ex.getMessage());
    }

    /**
     * Fallback handler for any unhandled exceptions.
     *
     * @param ex the unexpected exception
     * @return {@code 500 INTERNAL_SERVER_ERROR} with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
