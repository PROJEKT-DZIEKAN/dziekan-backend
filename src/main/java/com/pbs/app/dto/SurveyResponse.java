package com.pbs.app.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class SurveyResponse {
    private Long id;
    private String title;
    private String description;
    private Instant createdAt;
    private List<QuestionResponse> questions;
}
