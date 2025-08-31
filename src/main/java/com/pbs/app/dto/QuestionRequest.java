package com.pbs.app.dto;

import com.pbs.app.enums.QuestionType;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    private String text;
    private QuestionType type;
    private List<OptionRequest> surveyOptions;
}

