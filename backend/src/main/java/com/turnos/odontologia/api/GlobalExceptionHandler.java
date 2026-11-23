package com.turnos.odontologia.api;

import com.turnos.odontologia.api.dto.ApiError;
import com.turnos.odontologia.application.exception.AvailabilityConflictException;
import com.turnos.odontologia.application.exception.BusinessValidationException;
import com.turnos.odontologia.application.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("RESOURCE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler({BusinessValidationException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> validation(Exception exception) {
        String message = exception instanceof BusinessValidationException
                ? exception.getMessage()
                : "La solicitud contiene datos inválidos";
        return ResponseEntity.unprocessableEntity()
                .body(new ApiError("BUSINESS_VALIDATION", message));
    }

    @ExceptionHandler(AvailabilityConflictException.class)
    ResponseEntity<ApiError> conflict(AvailabilityConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(exception.getCode(), exception.getMessage()));
    }
}
