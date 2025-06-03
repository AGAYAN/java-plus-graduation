package ru.practicum.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RecommendedEvent {
    private Long id;
    private Double score;
}
