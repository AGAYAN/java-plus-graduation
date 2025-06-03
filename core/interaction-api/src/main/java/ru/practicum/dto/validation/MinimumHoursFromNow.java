package ru.practicum.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinimumHoursFromNowValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumHoursFromNow {

  String message() default "The date must be at least two hours in the future.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  long hoursInFuture() default 2;

}
