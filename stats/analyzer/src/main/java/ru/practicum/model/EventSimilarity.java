package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "similarities")
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id_a", nullable = false)
    private Long eventIdA;

    @Column(name = "event_id_b", nullable = false)
    private Long eventIdB;

    @Column(name = "similarity_score", nullable = false)
    private double similarityScore;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

}