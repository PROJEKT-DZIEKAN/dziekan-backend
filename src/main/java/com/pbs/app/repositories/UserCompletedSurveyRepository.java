package com.pbs.app.repositories;

import com.pbs.app.models.UserCompletedSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCompletedSurveyRepository extends JpaRepository<UserCompletedSurvey, Long> {

    boolean existsByUserIdAndSurveyId(Long userId, Long surveyId);

    Optional<UserCompletedSurvey> findByUserIdAndSurveyId(Long userId, Long surveyId);

    @Query("SELECT COUNT(ucs) FROM UserCompletedSurvey ucs WHERE ucs.user.id = :userId")
    long countCompletedSurveysByUserId(@Param("userId") Long userId);
}