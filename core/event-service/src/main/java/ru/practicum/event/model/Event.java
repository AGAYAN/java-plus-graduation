package ru.practicum.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import ru.practicum.category.model.Category;
import ru.practicum.dto.event.Location;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.enums.State;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false, nullable = false)
  private Long id;

  @Column(name = "annotation", length = 2000, nullable = false)
  private String annotation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "description", length = 7000, nullable = false)
  private String description;

  @Column(name = "event_date", nullable = false)
  private LocalDateTime eventDate;

  @Embedded
  @NotNull
  private Location location;

  @Column
  private Boolean paid;

  @Column(name = "created_on", nullable = false)
  private LocalDateTime createdOn;

  @Column(name = "initiator_id", nullable = false)
  private Long initiatorId;

  @Transient
  private UserDto initiator;

  @Column
  private Integer participantLimit = 0;

  @Column(name = "title", length = 120, nullable = false)
  private String title;

  @Column(name = "published_on")
  private LocalDateTime publishedOn;

  @Column(name = "request_moderation")
  private Boolean requestModeration = true;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private State state;

  @Transient
  private Integer confirmedRequests = 0;

  @Transient
  private Long views = 0L;
}
