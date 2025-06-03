package ru.practicum.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.dto.request.StatusRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = StatusRequestValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateStatusRequest {

  String message() default "Invalid participation request status.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  StatusRequest[] allowedValues() default {StatusRequest.CONFIRMED, StatusRequest.REJECTED};


}
