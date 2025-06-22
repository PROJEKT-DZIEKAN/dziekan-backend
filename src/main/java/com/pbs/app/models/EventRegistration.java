package com.pbs.app.models;

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
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Hidden
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Hidden
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User participant;

    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column
    private Boolean attended = false;

    // Można dodać więcej pól dotyczących rejestracji, te stopnie np :)
}