package com.pbs.app.dto;

import com.pbs.app.enums.QuestionType;
import lombok.Data;
import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String text;
    private QuestionType type;
    private List<OptionResponse> surveyOptions;
}
