package com.employee_managemet.employee_service.exception;

import com.employee_managemet.employee_service.constants.LogTags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataAccessResourceFailureException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

        String message = ex.getReason() != null
                ? ex.getReason()
                : "Request could not be processed";

        if (ex.getStatusCode().is4xxClientError()) {
            log.warn("[{}] {} - {}", LogTags.EXCEPTION,
                    ex.getStatusCode().value(), message);
        } else {
            log.error("[{}] {} - {}", LogTags.EXCEPTION,
                    ex.getStatusCode().value(), message, ex);
        }

        return buildResponse(
                ex.getStatusCode().value(),
                status != null ? status.getReasonPhrase() : "Request Error",
                message,
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        log.warn("[{}] Request validation failed: {}",
                LogTags.VALIDATION, errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "One or more request fields are invalid",
                request,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violation ->
                errors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        log.warn("[{}] Constraint violation: {}",
                LogTags.VALIDATION, errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Request validation failed",
                request,
                errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("[{}] Invalid request body", LogTags.VALIDATION);

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid request body or unsupported enum value",
                request,
                null
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Required parameter is missing: " + ex.getParameterName(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid value supplied for parameter: " + ex.getName(),
                request,
                null
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLocking(
            OptimisticLockingFailureException ex,
            HttpServletRequest request) {

        log.warn("[{}] Concurrent modification detected",
                LogTags.EXCEPTION);

        return buildResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "This record was modified by another request. Refresh the data and try again.",
                request,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("[{}] Database constraint violation",
                LogTags.DB_OP, ex);

        return buildResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "The operation conflicts with existing data or a database constraint.",
                request,
                null
        );
    }

    @ExceptionHandler({
            DataAccessResourceFailureException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<ApiErrorResponse> handleDatabaseUnavailable(
            Exception ex,
            HttpServletRequest request) {

        log.error("[{}] Database connection unavailable",
                LogTags.DB_OP, ex);

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "Database service is temporarily unavailable. Please try again later.",
                request,
                null
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseException(
            DataAccessException ex,
            HttpServletRequest request) {

        log.error("[{}] Database operation failed",
                LogTags.DB_OP, ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "A database operation could not be completed.",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("[{}] Unexpected application exception",
                LogTags.EXCEPTION, ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Contact support with the correlation ID.",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            int status,
            String error,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                request.getRequestURI(),
                MDC.get("correlationId"),
                validationErrors
        );

        return ResponseEntity.status(status).body(response);
    }
}