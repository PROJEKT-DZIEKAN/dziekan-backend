package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_completed_surveys",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "survey_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompletedSurvey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "completed_at")
    @CreationTimestamp
    @Builder.Default
    private Instant completedAt = Instant.now();
}