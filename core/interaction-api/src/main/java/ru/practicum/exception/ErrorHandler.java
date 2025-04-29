package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

  // 400 BAD REQUEST
  @ExceptionHandler({
          MethodArgumentNotValidException.class,
          MissingServletRequestParameterException.class,
          BadRequestException.class,
          HandlerMethodValidationException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiError> handleBadRequestExceptions(final RuntimeException exception) {
    log.warn("400 Bad Request: {}", exception.getMessage(), exception);
    return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  // 404 NOT FOUND
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException exception) {
    log.warn("404 Not Found: {}", exception.getMessage(), exception);
    return buildErrorResponse(exception, HttpStatus.NOT_FOUND, exception.getMessage());
  }

  // 409 CONFLICT
  @ExceptionHandler({ConflictException.class, AlreadyExistsException.class})
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ApiError> handleConflictExceptions(final RuntimeException exception) {
    log.warn("409 Conflict: {}", exception.getMessage(), exception);
    return buildErrorResponse(exception, HttpStatus.CONFLICT, exception.getMessage());
  }

  // 500 INTERNAL SERVER ERROR - на всякий случай для всех остальных ошибок
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiError> handleGenericException(final Exception exception) {
    log.error("500 Internal Server Error: {}", exception.getMessage(), exception);
    return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  private ResponseEntity<ApiError> buildErrorResponse(final Exception exception, HttpStatus status, String reason) {
    ApiError apiError = new ApiError(
            status,
            reason,
            exception.getMessage(),
            LocalDateTime.now(),
            ExceptionUtils.getStackTrace(exception)
    );
    return ResponseEntity.status(status).body(apiError);
  }
}
