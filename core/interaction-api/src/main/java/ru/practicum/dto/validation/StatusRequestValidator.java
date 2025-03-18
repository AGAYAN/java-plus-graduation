package ru.practicum.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.dto.request.StatusRequest;

import java.util.Arrays;
import java.util.List;

public class StatusRequestValidator implements ConstraintValidator<ValidateStatusRequest, StatusRequest> {

  private List<StatusRequest> validStatusOptions;

  @Override
  public void initialize(final ValidateStatusRequest constraintAnnotation) {
    validStatusOptions = Arrays.asList(constraintAnnotation.allowedValues());
  }

  @Override
  public boolean isValid(final StatusRequest statusValue, final ConstraintValidatorContext context) {
    if (statusValue == null) {
      return true;
    }
    return validStatusOptions.contains(statusValue);
  }
}
