package com.uchat.backend.common;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RestApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
        ) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();
        String message = details.isEmpty() ? "Validation failed." : details.getFirst();
        return ResponseEntity.badRequest().body(buildError(
            ApiErrorCode.VALIDATION_ERROR,
            message,
            request,
            details
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
        ) {
        List<String> details = exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();
        String message = details.isEmpty() ? "Validation failed." : details.getFirst();
        return ResponseEntity.badRequest().body(buildError(ApiErrorCode.VALIDATION_ERROR, message, request, details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
        ) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            ApiErrorCode.BAD_REQUEST,
                exception.getMessage(),
            Instant.now(),
            resolvePath(request),
            List.of()
        ));
    }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpServletRequest request
        ) {
        return ResponseEntity.badRequest().body(buildError(
            ApiErrorCode.BAD_REQUEST,
            "Malformed request body.",
            request,
            List.of()
        ));
        }

    @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        logger.error("Unhandled REST error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildError(ApiErrorCode.INTERNAL_ERROR, "Unexpected server error.", request, List.of()));
        }

        private String formatFieldError(FieldError error) {
        String message = Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid");
        return error.getField() + ": " + message;
        }

        private ApiErrorResponse buildError(
            ApiErrorCode code,
            String message,
            HttpServletRequest request,
            List<String> details
        ) {
        return new ApiErrorResponse(
            code,
            message,
            Instant.now(),
            resolvePath(request),
            details
        );
        }

        private String resolvePath(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null || request.getRequestURI().isBlank()) {
            return "unknown";
        }
        return request.getRequestURI();
    }
}
