package ru.practicum.model;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
public class RecommendedEvent {

    private Long id;
    private Double score;

    public RecommendedEvent(Long eventId, double maxResult) {
        this.id = eventId;
        this.score = maxResult;
    }

}
