package com.pbs.app.repositories;

import com.pbs.app.models.SurveyUserAnswer;
import com.pbs.app.models.SurveyUserAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyUserAnswerRepository extends JpaRepository<SurveyUserAnswer, SurveyUserAnswerId> {
    List<SurveyUserAnswer> findByUserId(Long userId);
    List<SurveyUserAnswer> findByQuestionId(Long questionId);
}