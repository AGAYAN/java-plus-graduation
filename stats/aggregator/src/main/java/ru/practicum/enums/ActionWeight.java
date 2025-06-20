package ru.practicum.enums;

public enum ActionWeight {
    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    final double value;

    ActionWeight(double value) {
        this.value = value;
    }
}