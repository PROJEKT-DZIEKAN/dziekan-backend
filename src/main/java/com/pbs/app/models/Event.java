package com.pbs.app.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table
@Data                       // generuje gettery, settery, toString, equals, hashCode
@NoArgsConstructor          // generuje konstruktor bezparametrowy
@AllArgsConstructor         // generuje konstruktor ze wszystkimi polami
public class Event {
    @Id
    @GeneratedValue
    @Column
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column
    private String title;

    @Size(max = 2000)
    @Column
    private String description;

    @NotNull
    @Column
    private LocalDateTime startTime;

    @NotNull
    @Column
    private LocalDateTime endTime;

    @NotBlank
    @Column
    @Size(max = 255)
    private String location;

    @Column
    private Double latitude;

    @Column
     private Double longitude;

    @Size(max = 255)
    private String qrcodeUrl;

    @Min(0)
    @Column
    private Integer maxParticipants;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();
}
