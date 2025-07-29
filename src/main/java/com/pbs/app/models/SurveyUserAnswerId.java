package com.pbs.app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyUserAnswerId implements Serializable {
    private Long userId;
    private Long questionId;
    private Long surveyOptionId;
}
