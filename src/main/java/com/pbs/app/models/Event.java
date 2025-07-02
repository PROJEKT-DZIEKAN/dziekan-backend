package com.pbs.app.models;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 255) @Column
    private String title;

    @Size(max = 2000) @Column
    private String description;

    @NotNull @Column
    private LocalDateTime startTime;

    @NotNull @Column
    private LocalDateTime endTime;

    @NotBlank @Size(max = 255) @Column
    private String location;

    @Column private Double latitude;
    @Column private Double longitude;

    @Size(max = 255) @Column
    private String qrcodeUrl;

    @Min(0) @Column
    private Integer maxParticipants;

    @Schema(hidden = true)
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = true)
    private User organizer;

    @Schema(hidden = true)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();

    @ManyToMany(mappedBy = "events")
    @Builder.Default
    private Set<Group> groups = new HashSet<>();
}
