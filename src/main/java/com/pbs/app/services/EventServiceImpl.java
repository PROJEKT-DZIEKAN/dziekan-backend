package com.pbs.app.services;

import com.pbs.app.models.Event;
import com.pbs.app.models.User;
import com.pbs.app.repositories.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Event createEvent(Event event)
    {
        event.setId(null); // Ensure the ID is null to use the generator mother fucker nicolas
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, Event event)
    {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event with id " + id + " does not exist.");
        }
        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event with id " + id + " does not exist.");
        }
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Event getEventById(Long id)
    {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + id + " does not exist."));
    }

    @Override
    @Transactional
    public List<Event> getAllEvents()
    {
        return eventRepository.findAll();
    }

    @Override
    @Transactional
    public List<Event> getEventsStartTimeBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartTimeBetween(start, end);
    }

    @Override
    @Transactional
    public List<Event> findByStartTime(LocalDateTime start) {
        return eventRepository.findByStartTime(start);
    }

    @Override
    @Transactional
    public List<Event> findByEndTime(LocalDateTime end) {
        return eventRepository.findByEndTime(end);
    }

    @Override
    @Transactional
    public List<Event> findUpcomingEvents(LocalDateTime now) {
        return eventRepository.findByStartTimeAfter(now);
    }

    @Override
    @Transactional
    public List<Event> findByTitleContainingIgnoreCase(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional
    public List<Event> findByLocationContainingIgnoreCase(String location) {
        return eventRepository.findByLocationContainingIgnoreCase(location);
    }

    @Override
    @Transactional
    public List<Event> findByDescriptionContainingIgnoreCase(String description) {
        return eventRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    @Transactional
    public List<Event> findByOrganizer(User organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    @Override
    @Transactional
    public List<Event> findEventsWithAvailableSpots() {
        return eventRepository.findEventsWithAvailableSpots();
    }


}
