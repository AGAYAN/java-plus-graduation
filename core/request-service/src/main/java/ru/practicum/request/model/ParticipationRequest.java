package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import ru.practicum.dto.request.StatusRequest;

import java.time.LocalDateTime;


@Entity
@Table(name = "request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class ParticipationRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false)
  private Long event;

  @Column(name = "user_id", nullable = false)
  private Long requester;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StatusRequest status = StatusRequest.PENDING;

  private LocalDateTime created = LocalDateTime.now();

  public ParticipationRequest(final Long user, final Long event) {
    this.requester = user;
    this.event = event;
    this.status = StatusRequest.PENDING;
  }
}
