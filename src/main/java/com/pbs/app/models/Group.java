package com.pbs.app.models;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Id @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false) @Size(max = 255)
    private String name;

    @Column @Size(max = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Min(0) @Column
    private Integer maxParticipants;

    @Schema(hidden = true)
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = true)
    private User organizer;

    @ManyToMany(mappedBy = "groups")
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @ManyToMany
    @JoinTable(
      name = "group_event",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @Builder.Default
    private Set<Event> events = new HashSet<>();
}
