package com.pbs.app.services;

import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.Event;
import com.pbs.app.models.EventRegistration;
import com.pbs.app.models.User;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationService {
    List<EventRegistration> getRegistrationsByEventId(Long eventId);
    List<EventRegistration> getRegistrationsByParticipantId(Long userId);
    List<EventRegistration> getRegistrationsByStatus(RegistrationStatus status);
    Optional<EventRegistration> getRegistrationByEventAndParticipant(Event event, User participant);
    EventRegistration createRegistration(EventRegistration eventRegistration);
    List<EventRegistration> createManyRegistrations(List<EventRegistration> registrations);
    EventRegistration updateRegistration(Long id, EventRegistration eventRegistration);
    void deleteRegistration(Long id);

    void updateRegistrationStatus(Long id, RegistrationStatus status);
}
