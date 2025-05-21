package ru.practicum.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.dto.event.enums.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = StateActionValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStateAction {

  String message() default "Invalid state action.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  Role role() default Role.USER;
}
