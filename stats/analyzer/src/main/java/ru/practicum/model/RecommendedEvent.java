package ru.practicum.model;

import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
public class RecommendedEvent {

    private Long id;
    private Double similarityScore;

    public RecommendedEvent(Long eventId, Double similarityScore) {
        this.id = eventId;
        this.similarityScore = similarityScore;
    }

}
