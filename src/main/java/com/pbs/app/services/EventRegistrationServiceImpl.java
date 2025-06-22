package com.pbs.app.services;

import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.Event;
import com.pbs.app.models.User;
import com.pbs.app.repositories.EventRegistrationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.pbs.app.models.EventRegistration;

@Service
public class EventRegistrationServiceImpl implements EventRegistrationService {
    private final EventRegistrationRepository eventRegistrationRepository;

    public EventRegistrationServiceImpl(EventRegistrationRepository eventRegistrationRepository) {
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    @Override
    @Transactional
    public List<EventRegistration> getRegistrationsByEventId(Long eventId) {
        return eventRegistrationRepository.findByEventId(eventId);
    }

    @Override
    @Transactional
    public List<EventRegistration> getRegistrationsByParticipantId(Long userId) {
        return eventRegistrationRepository.findByParticipantId(userId);
    }

    @Override
    @Transactional
    public List<EventRegistration> getRegistrationsByStatus(RegistrationStatus status) {
        return eventRegistrationRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Optional<EventRegistration> getRegistrationByEventAndParticipant(Event event, User participant) {
        return eventRegistrationRepository.findByEventAndParticipant(event, participant);
    }

    @Override
    @Transactional
    public EventRegistration createRegistration(EventRegistration eventRegistration) {
        eventRegistration.setId(null); // Ensure the ID is null to use the generator
        return eventRegistrationRepository.save(eventRegistration);
    }

    @Override
    @Transactional
    public EventRegistration updateRegistration(Long id, EventRegistration eventRegistration) {
        return eventRegistrationRepository.findById(id).map(existingRegistration -> {
            existingRegistration.setEvent(eventRegistration.getEvent());
            existingRegistration.setParticipant(eventRegistration.getParticipant());
            existingRegistration.setStatus(eventRegistration.getStatus());
            existingRegistration.setStatusUpdatedAt(eventRegistration.getStatusUpdatedAt());
            return eventRegistrationRepository.save(existingRegistration);
        }).orElseThrow(() -> new RuntimeException("Event Registration not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteRegistration(Long id) {
        if (!eventRegistrationRepository.existsById(id)) {
            throw new RuntimeException("Event Registration not found with id: " + id);
        }
        eventRegistrationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateRegistrationStatus(Long id, RegistrationStatus status) {
        eventRegistrationRepository.updateStatusById(id, status);
    }
}