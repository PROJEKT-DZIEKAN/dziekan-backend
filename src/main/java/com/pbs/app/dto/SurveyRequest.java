package com.pbs.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class SurveyRequest {
    private Long id;
    private String title;
    private String description;
    private List<QuestionRequest> questions;
}