package com.pbs.app.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pbs.app.enums.RegistrationStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Hidden
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference("event-registrations")
    private Event event;
    
    @Hidden
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-registrations")
    private User participant;

    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column
    private LocalDateTime statusUpdatedAt;
}