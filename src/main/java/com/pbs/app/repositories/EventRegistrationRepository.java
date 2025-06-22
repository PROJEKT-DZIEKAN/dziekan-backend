package com.pbs.app.repositories;

import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.Event;
import com.pbs.app.models.EventRegistration;
import com.pbs.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByEventId(Long eventId);
    List<EventRegistration> findByParticipantId(Long userId);
    List<EventRegistration> findByStatus(RegistrationStatus status);
    Optional<EventRegistration> findByEventAndParticipant(Event event, User participant);

    @Modifying
    @Transactional
    @Query("UPDATE EventRegistration e SET e.status = :status, e.statusUpdatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void updateStatusById(@Param("id") Long id, @Param("status") RegistrationStatus status);
}

