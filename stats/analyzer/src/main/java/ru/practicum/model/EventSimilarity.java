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

    @Column(name = "event_id_a")
    private Long eventIdA;

    @Column(name = "event_id_b")
    private Long eventIdB;

    @Column(name = "result")
    private double maxResult;

    @Column(name = "time")
    private LocalDateTime time;

}
