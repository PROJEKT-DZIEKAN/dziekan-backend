package com.pbs.app.models;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "\"group\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) @Size(max = 255)
    private String name;

    @Column @Size(max = 2000)
    private String description;

    @Column(nullable = true)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Min(0) @Column
    private Integer maxParticipants;

    @ManyToMany(mappedBy = "groups")
    @Builder.Default
    @JsonIgnore
    private Set<User> participants = new HashSet<>();

    @ManyToMany
    @JoinTable(
      name = "group_event",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @Builder.Default
    @JsonIgnore
    private Set<Event> events = new HashSet<>();
}
