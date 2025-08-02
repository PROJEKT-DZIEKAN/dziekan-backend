package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_answers")
@IdClass(SurveyUserAnswerId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyUserAnswer {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @Id
    @Column(name = "survey_option_id")
    private Long surveyOptionId;

    @Column(name = "answered_at", nullable = false, updatable = false)
    private Instant answeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private SurveyQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_option_id", insertable = false, updatable = false)
    private SurveyOption surveyOption;
}
