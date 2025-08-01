package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "survey_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @Column(nullable = false, length = 500)
    private String text;
}
