package com.pbs.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnswerRequest {
    private Long questionId;
    private List<Long> selectedOptionIds;
}