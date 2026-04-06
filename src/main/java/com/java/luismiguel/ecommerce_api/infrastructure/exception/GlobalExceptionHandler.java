package com.java.luismiguel.ecommerce_api.infrastructure.exception;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.AuthException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart.CartException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.ProductException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static HashMap<String, Object> errorBuilder(HttpStatus status, String errorName, String exceptionMessage){
        HashMap<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error",  errorName);
        body.put("message", exceptionMessage);
        return body;
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException e) {
        HashMap<String, Object> body = errorBuilder(
                e.getStatus(),
                e.getStatus().name(),
                e.getMessage()
        );
        return new ResponseEntity<>(body, e.getStatus());
    }


    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Object> handleRateLimitExceeded(RequestNotPermitted ex) {
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                "Rate limit exceeded. Please try again later."
        );
        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception){
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
