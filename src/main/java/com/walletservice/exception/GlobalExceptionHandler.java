package com.walletservice.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(EventSerializationException.class)
    public ResponseEntity<ErrorResponse> handleEventSerialization(ValidationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage(), request);
    }

    // Maps @Valid field errors to specific ErrorCodes from the spec
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Optional<FieldError> firstError = ex.getBindingResult().getFieldErrors().stream().findFirst();

        if (firstError.isPresent()) {
            String field = firstError.get().getField();
            String message = firstError.get().getDefaultMessage();

            ErrorCode errorCode = switch (field) {
                case "amount" -> ErrorCode.INVALID_AMOUNT;
                case "description" -> ErrorCode.DESCRIPTION_MISSING;
                default -> ErrorCode.INVALID_PARAMETER;
            };

            return buildErrorResponse(HttpStatus.BAD_REQUEST, errorCode, message, request);
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER,
                "Validation failed", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER,
                "Invalid value in request body", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNEXPECTED_ERROR,
                "Unexpected server error", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, ErrorCode errorCode,
                                                             String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .errorCode(String.valueOf(errorCode))
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
