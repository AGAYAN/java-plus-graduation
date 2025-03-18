package ru.practicum.comment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private Long user;

  @JoinColumn(name = "event_id", nullable = false)
  @JsonIgnore
  private Long eventId;

  @Column(nullable = false, length = 5000)
  private String content;

  @Column(name = "is_initiator", nullable = false)
  private boolean isInitiator = false;

  @Column(name = "created", nullable = false)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime created = LocalDateTime.now();

}
