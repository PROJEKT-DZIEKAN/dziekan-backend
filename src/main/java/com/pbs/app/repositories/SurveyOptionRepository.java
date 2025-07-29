package com.pbs.app.repositories;


import com.pbs.app.models.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {
    List<SurveyOption> findByQuestionId(Long questionId);
}
