package com.pbs.app.repositories;

import com.pbs.app.models.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

        @Query("""
        SELECT s FROM Survey s
        LEFT JOIN UserCompletedSurvey ucs
            ON ucs.survey.id = s.id AND ucs.user.id = :userId
        WHERE s.isActive = true AND ucs.id IS NULL
        ORDER BY s.createdAt DESC
    """)
    List<Survey> findAvailableSurveysForUser(@Param("userId") Long userId);


    @Query("SELECT s, " +
           "CASE WHEN EXISTS (SELECT 1 FROM UserCompletedSurvey ucs " +
           "WHERE ucs.survey.id = s.id AND ucs.user.id = :userId) " +
           "THEN true ELSE false END as completed " +
           "FROM Survey s WHERE s.isActive = true")
    List<Object[]> findAllSurveysWithCompletionStatus(@Param("userId") Long userId);
}
