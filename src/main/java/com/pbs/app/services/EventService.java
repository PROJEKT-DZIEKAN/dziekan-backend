package com.pbs.app.services;

import com.pbs.app.models.Event;
import com.pbs.app.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    Event createEvent(Event event);

    Event updateEvent(Long id, Event event);

    void deleteEvent(Long id);

    Event getEventById(Long id);

    List<Event> getAllEvents();

    List<Event> getEventsStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByStartTime(LocalDateTime start);

    List<Event> findByEndTime(LocalDateTime end);

    List<Event> findUpcomingEvents(LocalDateTime now);

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    List<Event> findByLocationContainingIgnoreCase(String location);

    List<Event> findByDescriptionContainingIgnoreCase(String description);

    List<Event> findByOrganizer(User organizer);

    List<Event> findEventsWithAvailableSpots();
}
