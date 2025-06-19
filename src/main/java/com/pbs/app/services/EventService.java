package com.pbs.app.services;

import com.pbs.app.models.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    Event createEvent(Event event);

    Event updateEvent(Long id, Event event);

    void deleteEvent(Long id);

    List<Event> getAllEvents();

    List<Event> getEventsStartTimeBetween(LocalDateTime start, LocalDateTime end);
}

// WORK IN PROGRESS YOU STUPID FUCKING IDIOT
