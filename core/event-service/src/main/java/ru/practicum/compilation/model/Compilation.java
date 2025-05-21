package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.event.model.Event;

import java.util.Set;

@Entity
@Table(name = "compilation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Compilation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false, unique = true)
  private String title;

  @Column(name = "pinned", nullable = false, columnDefinition = "boolean default false")
  private boolean pinned;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinTable(name = "compilation_event",
      joinColumns = @JoinColumn(name = "compilation_id"),
      inverseJoinColumns = @JoinColumn(name = "event_id"))
  private Set<Event> events;
}
