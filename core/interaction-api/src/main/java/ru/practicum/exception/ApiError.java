package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ApiError(HttpStatus status,
                       String reason,
                       String message,
                       @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                       LocalDateTime timestamp,
                       String errors) {

}
