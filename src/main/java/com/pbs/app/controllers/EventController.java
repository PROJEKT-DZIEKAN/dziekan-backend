package com.pbs.app.controllers;


import com.pbs.app.models.Event;
import com.pbs.app.repositories.EventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventRepository eventRepository;

    @PostMapping("/create")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event createdEvent = eventRepository.save(event);
        return ResponseEntity.ok(createdEvent);
    }

    // TO BE COMPLETED: Implement other CRUD operations for EventController

}
