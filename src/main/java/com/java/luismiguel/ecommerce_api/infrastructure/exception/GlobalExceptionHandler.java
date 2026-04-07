package com.java.luismiguel.ecommerce_api.infrastructure.exception;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static HashMap<String, Object> errorBuilder(HttpStatus status, String errorName, String exceptionMessage, String path){
        HashMap<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error",  errorName);
        body.put("message", exceptionMessage);
        body.put("path", path);
        return body;
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        HashMap<String, Object> body = errorBuilder(
                e.getStatus(),
                e.getStatus().name(),
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, e.getStatus());
    }


    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Object> handleRateLimitExceeded(RequestNotPermitted ex, HttpServletRequest request) {
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception, HttpServletRequest request){
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
