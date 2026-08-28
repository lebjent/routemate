package com.trip.routemate.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Objects;

/** API 오류 형식을 RFC 9457 Problem Details로 통일합니다. */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException exception) {
        var problem = exception.getBody();
        addTraceId(problem);
        return ResponseEntity.status(exception.getStatusCode()).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        var errors = new LinkedHashMap<String, String>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        var problem = problem(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요.");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "요청 형식 또는 입력값을 확인해 주세요."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled API error: {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.internalServerError().body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다."));
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(Objects.requireNonNull(status), detail);
        addTraceId(problem);
        return problem;
    }

    private void addTraceId(ProblemDetail problem) {
        var traceId = MDC.get("traceId");
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
    }
}
