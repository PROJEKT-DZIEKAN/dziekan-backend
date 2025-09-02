package com.pbs.app.services;

import com.pbs.app.dto.BulkRegistrationDTO;
import com.pbs.app.dto.EventRegistrationDTO;
import com.pbs.app.dto.RegistrationResponseDTO;
import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.Event;
import com.pbs.app.models.EventRegistration;
import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.repositories.EventRegistrationRepository;
import com.pbs.app.repositories.EventRepository;
import com.pbs.app.repositories.GroupRepository;
import com.pbs.app.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public EventService(EventRepository eventRepository,
                       UserRepository userRepository,
                       EventRegistrationRepository eventRegistrationRepository, GroupRepository groupRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public Event createEvent(Event event) {
        event.setId(null);
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, Event event) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event with id " + id + " does not exist.");
        }
        event.setId(id);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event with id " + id + " does not exist.");
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + id + " does not exist."));
    }

    @Transactional
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional
    public List<RegistrationResponseDTO> registerGroupToEvent(Long eventId, Long groupId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        Set<User> groupParticipants = group.getParticipants();

        if (groupParticipants.isEmpty()) {
            throw new IllegalStateException("Group has no participants to register");
        }

        List<RegistrationResponseDTO> responses = new ArrayList<>();

        if (event.getMaxParticipants() != null) {
            long currentParticipants = eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
            long requestedRegistrations = groupParticipants.size();

            if (currentParticipants + requestedRegistrations > event.getMaxParticipants()) {
                long availableSpots = event.getMaxParticipants() - currentParticipants;
                throw new IllegalStateException(
                    String.format("Cannot register group with %d members. Only %d spots available.",
                                requestedRegistrations, availableSpots)
                );
            }
        }

        event.getGroups().add(group);
        group.getEvents().add(event);


        for (User user : groupParticipants) {
            try {
                boolean alreadyRegistered = eventRegistrationRepository
                        .existsByEventAndParticipant(event, user);

                if (alreadyRegistered) {
                    responses.add(new RegistrationResponseDTO(
                        user.getId(),
                        false,
                        "User already registered for this event"
                    ));
                    continue;
                }

                EventRegistration registration = new EventRegistration();
                registration.setEvent(event);
                registration.setParticipant(user);
                registration.setRegisteredAt(LocalDateTime.now());
                registration.setStatus(RegistrationStatus.REGISTERED);
                registration.setStatusUpdatedAt(LocalDateTime.now());

                eventRegistrationRepository.save(registration);

                responses.add(new RegistrationResponseDTO(
                    user.getId(),
                    true,
                    "Successfully registered as part of group: " + group.getName()
                ));

            } catch (Exception e) {
                responses.add(new RegistrationResponseDTO(
                    user.getId(),
                    false,
                    "Registration failed: " + e.getMessage()
                ));
            }
        }

        return responses;
    }

    @Transactional
    public List<Event> getEventsStartTimeBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartTimeBetween(start, end);
    }

    @Transactional
    public List<Event> findByStartTime(LocalDateTime start) {
        return eventRepository.findByStartTime(start);
    }

    @Transactional
    public List<Event> findByEndTime(LocalDateTime end) {
        return eventRepository.findByEndTime(end);
    }

    @Transactional
    public List<Event> findUpcomingEvents(LocalDateTime now) {
        return eventRepository.findByStartTimeAfter(now);
    }

    @Transactional
    public List<Event> findByTitleContainingIgnoreCase(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Transactional
    public List<Event> findByLocationContainingIgnoreCase(String location) {
        return eventRepository.findByLocationContainingIgnoreCase(location);
    }

    @Transactional
    public List<Event> findByDescriptionContainingIgnoreCase(String description) {
        return eventRepository.findByDescriptionContainingIgnoreCase(description);
    }


    @Transactional
    public List<Event> findEventsWithAvailableSpots() {
        return eventRepository.findEventsWithAvailableSpots();
    }

    @Transactional
    public EventRegistrationDTO registerUserToEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " does not exist."));

        boolean alreadyRegistered = eventRegistrationRepository
                .existsByEventAndParticipant(event, user);

        if (alreadyRegistered) {
            throw new IllegalStateException("User " + userId + " is already registered for event " + eventId);
        }

        if (event.getMaxParticipants() != null) {
            long currentParticipants = eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
            if (currentParticipants >= event.getMaxParticipants()) {
                throw new IllegalStateException("Event has reached maximum number of participants");
            }
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setParticipant(user);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.REGISTERED);
        registration.setStatusUpdatedAt(LocalDateTime.now());

        EventRegistration saved = eventRegistrationRepository.save(registration);

        return convertToDTO(saved);
    }

    @Transactional
    public List<RegistrationResponseDTO> registerMultipleUsersToEvent(Long eventId, BulkRegistrationDTO bulkRegistration) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        List<RegistrationResponseDTO> responses = new ArrayList<>();

        if (event.getMaxParticipants() != null) {
            long currentParticipants = eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
            long requestedRegistrations = bulkRegistration.getUserIds().size();

            if (currentParticipants + requestedRegistrations > event.getMaxParticipants()) {
                long availableSpots = event.getMaxParticipants() - currentParticipants;
                throw new IllegalStateException(
                    String.format("Cannot register %d users. Only %d spots available.",
                                requestedRegistrations, availableSpots)
                );
            }
        }

        for (Long userId : bulkRegistration.getUserIds()) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " does not exist."));

                boolean alreadyRegistered = eventRegistrationRepository
                        .existsByEventAndParticipant(event, user);

                if (alreadyRegistered) {
                    responses.add(new RegistrationResponseDTO(
                        userId,
                        false,
                        "User already registered for this event"
                    ));
                    continue;
                }
                EventRegistration registration = new EventRegistration();
                registration.setEvent(event);
                registration.setParticipant(user);
                registration.setRegisteredAt(LocalDateTime.now());
                registration.setStatus(RegistrationStatus.REGISTERED);
                registration.setStatusUpdatedAt(LocalDateTime.now());

                eventRegistrationRepository.save(registration);

                responses.add(new RegistrationResponseDTO(
                    userId,
                    true,
                    "Successfully registered"
                ));

            } catch (EntityNotFoundException e) {
                responses.add(new RegistrationResponseDTO(
                    userId,
                    false,
                    e.getMessage()
                ));
            } catch (Exception e) {
                responses.add(new RegistrationResponseDTO(
                    userId,
                    false,
                    "Registration failed: " + e.getMessage()
                ));
            }
        }

        return responses;
    }

    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " does not exist."));

        EventRegistration registration = eventRegistrationRepository
                .findByEventAndParticipant(event, user)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Registration not found for user " + userId + " and event " + eventId
                ));

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setStatusUpdatedAt(LocalDateTime.now());
        eventRegistrationRepository.save(registration);
    }

    @Transactional
    public List<RegistrationResponseDTO> cancelMultipleRegistrations(Long eventId, BulkRegistrationDTO bulkRegistration) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        List<RegistrationResponseDTO> responses = new ArrayList<>();

        for (Long userId : bulkRegistration.getUserIds()) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " does not exist."));

                EventRegistration registration = eventRegistrationRepository
                        .findByEventAndParticipant(event, user)
                        .orElseThrow(() -> new EntityNotFoundException(
                            "Registration not found for user " + userId
                        ));

                registration.setStatus(RegistrationStatus.CANCELLED);
                registration.setStatusUpdatedAt(LocalDateTime.now());
                eventRegistrationRepository.save(registration);

                responses.add(new RegistrationResponseDTO(
                    userId,
                    true,
                    "Registration cancelled successfully"
                ));

            } catch (EntityNotFoundException e) {
                responses.add(new RegistrationResponseDTO(
                    userId,
                    false,
                    e.getMessage()
                ));
            } catch (Exception e) {
                responses.add(new RegistrationResponseDTO(
                    userId,
                    false,
                    "Cancellation failed: " + e.getMessage()
                ));
            }
        }

        return responses;
    }

    @Transactional
    public List<EventRegistrationDTO> getEventRegistrations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        return event.getRegistrations().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EventRegistrationDTO> getRegisteredUsers(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        return eventRegistrationRepository
                .findByEventAndStatus(event, RegistrationStatus.REGISTERED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean isUserRegistered(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " does not exist."));

        return eventRegistrationRepository.existsByEventAndParticipantAndStatus(
            event, user, RegistrationStatus.REGISTERED
        );
    }


    @Transactional
    public long getRegisteredParticipantsCount(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        return eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
    }

    @Transactional
    public Integer getAvailableSpots(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " does not exist."));

        if (event.getMaxParticipants() == null) {
            return null;
        }

        long registered = eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
        return (int) (event.getMaxParticipants() - registered);
    }

    private EventRegistrationDTO convertToDTO(EventRegistration registration) {
        EventRegistrationDTO dto = new EventRegistrationDTO();
        dto.setId(registration.getId());
        dto.setEventId(registration.getEvent().getId());
        dto.setEventTitle(registration.getEvent().getTitle());
        dto.setUserId(registration.getParticipant().getId());
        dto.setUserFirstName(registration.getParticipant().getFirstName());
        dto.setUserSurname(registration.getParticipant().getSurname());
        dto.setUserEmail(registration.getParticipant().getEmail());
        dto.setRegisteredAt(registration.getRegisteredAt());
        dto.setStatus(registration.getStatus());
        dto.setStatusUpdatedAt(registration.getStatusUpdatedAt());
        return dto;
    }
}