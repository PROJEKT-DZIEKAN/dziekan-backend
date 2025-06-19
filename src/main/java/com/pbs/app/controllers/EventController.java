package com.pbs.app.controllers;

import com.pbs.app.models.Event;
import com.pbs.app.models.User;
import com.pbs.app.services.EventServiceImpl;
import com.pbs.app.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventServiceImpl eventService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.ok(createdEvent);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event event) {
        try {
            Event updatedEvent = eventService.updateEvent(id, event);
            return ResponseEntity.ok(updatedEvent);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/between")
    public ResponseEntity<List<Event>> getEventsBetween(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<Event> events = eventService.getEventsStartTimeBetween(start, end);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie po dacie rozpoczęcia
    @GetMapping("/by-start-time")
    public ResponseEntity<List<Event>> getEventsByStartTime(
            @RequestParam LocalDateTime startTime) {
        List<Event> events = eventService.findByStartTime(startTime);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie po dacie zakończenia
    @GetMapping("/by-end-time")
    public ResponseEntity<List<Event>> getEventsByEndTime(
            @RequestParam LocalDateTime endTime) {
        List<Event> events = eventService.findByEndTime(endTime);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie nadchodzących wydarzeń
    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents(
            @RequestParam(required = false) LocalDateTime now) {
        if (now == null) {
            now = LocalDateTime.now();
        }
        List<Event> events = eventService.findUpcomingEvents(now);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie po tytule
    @GetMapping("/by-title")
    public ResponseEntity<List<Event>> getEventsByTitle(
            @RequestParam String keyword) {
        List<Event> events = eventService.findByTitleContainingIgnoreCase(keyword);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie po lokalizacji
    @GetMapping("/by-location")
    public ResponseEntity<List<Event>> getEventsByLocation(
            @RequestParam String location) {
        List<Event> events = eventService.findByLocationContainingIgnoreCase(location);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie po opisie
    @GetMapping("/by-description")
    public ResponseEntity<List<Event>> getEventsByDescription(
            @RequestParam String description) {
        List<Event> events = eventService.findByDescriptionContainingIgnoreCase(description);
        return ResponseEntity.ok(events);
    }

    // Wyszukiwanie po organizatorze
    @GetMapping("/by-organizer/{organizerId}")
    public ResponseEntity<List<Event>> getEventsByOrganizer(
            @PathVariable Long organizerId) {
        try {
            User organizer = userService.getUserById(organizerId)
                    .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o ID: " + organizerId));
            List<Event> events = eventService.findByOrganizer(organizer);
            return ResponseEntity.ok(events);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Wyszukiwanie wydarzeń z dostępnymi miejscami
    @GetMapping("/with-available-spots")
    public ResponseEntity<List<Event>> getEventsWithAvailableSpots() {
        List<Event> events = eventService.findEventsWithAvailableSpots();
        return ResponseEntity.ok(events);
    }
}