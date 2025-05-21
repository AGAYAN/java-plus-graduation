package ru.practicum.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.dto.event.enums.Role;
import ru.practicum.dto.event.enums.StateAction;


import java.util.List;

public class StateActionValidator implements ConstraintValidator<ValidStateAction, String> {

  private List<String> validStateOptions;

  @Override
  public void initialize(final ValidStateAction constraintAnnotation) {
    final Role role = constraintAnnotation.role();
    validStateOptions = StateAction.getValidStates(role);
  }

  @Override
  public boolean isValid(final String stateActionValue, final ConstraintValidatorContext context) {
    if (stateActionValue == null || stateActionValue.isEmpty()) {
      return true;
    }
    return validStateOptions.contains(stateActionValue.trim().toUpperCase());
  }
}
