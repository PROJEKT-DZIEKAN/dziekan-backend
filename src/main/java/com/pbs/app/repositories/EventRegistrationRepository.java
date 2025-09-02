package com.pbs.app.repositories;

import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.Event;
import com.pbs.app.models.EventRegistration;
import com.pbs.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventAndParticipant(Event event, User participant);

    List<EventRegistration> findByEvent(Event event);

    List<EventRegistration> findByParticipant(User participant);

    List<EventRegistration> findByEventAndStatus(Event event, RegistrationStatus status);

    boolean existsByEventAndParticipant(Event event, User participant);

    boolean existsByEventAndParticipantAndStatus(Event event, User participant, RegistrationStatus status);

    long countByEvent(Event event);

    long countByEventAndStatus(Event event, RegistrationStatus status);

    @Query("SELECT er FROM EventRegistration er " +
           "WHERE er.participant = :user " +
           "AND er.status = :status " +
           "ORDER BY er.event.startTime")
    List<EventRegistration> findUserRegisteredEvents(@Param("user") User user,
                                                     @Param("status") RegistrationStatus status);

    @Query("SELECT er FROM EventRegistration er " +
           "WHERE er.participant = :user " +
           "AND er.status = 'REGISTERED' " +
           "AND er.event.startTime > CURRENT_TIMESTAMP " +
           "ORDER BY er.event.startTime")
    List<EventRegistration> findUserUpcomingEvents(@Param("user") User user);

    void deleteByEvent(Event event);

    void deleteByParticipant(User participant);
}