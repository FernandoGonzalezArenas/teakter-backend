package com.teakter.user.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex){
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex){
        Map<String, String> errores=new HashMap<>();
        ex.getConstraintViolations().forEach(violations ->
                errores.put(violations.getPropertyPath().toString(), violations.getMessage()) );
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validacion en parametros", errores);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationError(MethodArgumentNotValidException ex){
        Map<String, String> errores=new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "validacion fallida", errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex){
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor", null);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, Map<String, String> fieldErrors){
        Map<String, Object> body=new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (fieldErrors != null && !fieldErrors.isEmpty()){
            body.put("fieldErrors", fieldErrors);
        }
        return new ResponseEntity<>(body, status);
    }

}
