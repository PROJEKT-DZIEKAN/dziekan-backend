package com.pbs.app.controllers;

import com.pbs.app.dto.BulkRegistrationDTO;
import com.pbs.app.dto.EventRegistrationDTO;
import com.pbs.app.dto.RegistrationResponseDTO;
import com.pbs.app.models.Event;
import com.pbs.app.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hibernate.sql.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Registration", description = "Event registration management endpoints")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/create")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long eventId, @RequestBody Event event) {
        Event updatedEvent = eventService.updateEvent(eventId, event);
        return ResponseEntity.ok(updatedEvent);
    }


    @PostMapping("/{eventId}/register/{userId}")
    @Operation(summary = "Register single user to event")
    public ResponseEntity<EventRegistrationDTO> registerUser(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        EventRegistrationDTO registration = eventService.registerUserToEvent(eventId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(registration);
    }

    @PostMapping("/{eventId}/register-multiple")
    @Operation(summary = "Register multiple users to event")
    public ResponseEntity<List<RegistrationResponseDTO>> registerMultipleUsers(
            @PathVariable Long eventId,
            @Valid @RequestBody BulkRegistrationDTO bulkRegistration) {
        List<RegistrationResponseDTO> responses = eventService.registerMultipleUsersToEvent(eventId, bulkRegistration);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{eventId}/register-group/{groupId}")
    @Operation(summary = "Register all group members to an event",
               description = "Registers all participants of a group to the specified event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group registration processed successfully"),
        @ApiResponse(responseCode = "404", description = "Event or group not found"),
        @ApiResponse(responseCode = "400", description = "Registration failed - insufficient spots or other error")
    })
    public ResponseEntity<List<RegistrationResponseDTO>> registerGroupToEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable Long eventId,
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        List<RegistrationResponseDTO> responses = eventService.registerGroupToEvent(eventId, groupId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{eventId}/unregister/{userId}")
    @Operation(summary = "Cancel user registration")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        eventService.cancelRegistration(eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/unregister-multiple")
    @Operation(summary = "Cancel multiple user registrations")
    public ResponseEntity<List<RegistrationResponseDTO>> cancelMultipleRegistrations(
            @PathVariable Long eventId,
            @Valid @RequestBody BulkRegistrationDTO bulkRegistration) {
        List<RegistrationResponseDTO> responses = eventService.cancelMultipleRegistrations(eventId, bulkRegistration);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{eventId}/registrations")
    @Operation(summary = "Get all registrations for event")
    public ResponseEntity<List<EventRegistrationDTO>> getEventRegistrations(@PathVariable Long eventId) {
        List<EventRegistrationDTO> registrations = eventService.getEventRegistrations(eventId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{eventId}/registered-users")
    @Operation(summary = "Get registered users for event")
    public ResponseEntity<List<EventRegistrationDTO>> getRegisteredUsers(@PathVariable Long eventId) {
        List<EventRegistrationDTO> registrations = eventService.getRegisteredUsers(eventId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{eventId}/is-registered/{userId}")
    @Operation(summary = "Check if user is registered for event")
    public ResponseEntity<Boolean> isUserRegistered(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        boolean isRegistered = eventService.isUserRegistered(eventId, userId);
        return ResponseEntity.ok(isRegistered);
    }

    @GetMapping("/{eventId}/participants-count")
    @Operation(summary = "Get number of registered participants")
    public ResponseEntity<Long> getParticipantsCount(@PathVariable Long eventId) {
        long count = eventService.getRegisteredParticipantsCount(eventId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{eventId}/available-spots")
    @Operation(summary = "Get number of available spots")
    public ResponseEntity<Integer> getAvailableSpots(@PathVariable Long eventId) {
        Integer spots = eventService.getAvailableSpots(eventId);
        return ResponseEntity.ok(spots);
    }
    // NICOLAS LEAVE MY BELOVED CONTROLLERS ALONE PLEASE
    @GetMapping("/between")
    public ResponseEntity<List<Event>> getEventsBetween(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<Event> events = eventService.getEventsStartTimeBetween(start, end);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-start-time")
    public ResponseEntity<List<Event>> getEventsByStartTime(
            @RequestParam LocalDateTime startTime) {
        List<Event> events = eventService.findByStartTime(startTime);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-end-time")
    public ResponseEntity<List<Event>> getEventsByEndTime(
            @RequestParam LocalDateTime endTime) {
        List<Event> events = eventService.findByEndTime(endTime);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents(
            @RequestParam(required = false) LocalDateTime now) {
        if (now == null) {
            now = LocalDateTime.now();
        }
        List<Event> events = eventService.findUpcomingEvents(now);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-title")
    public ResponseEntity<List<Event>> getEventsByTitle(
            @RequestParam String keyword) {
        List<Event> events = eventService.findByTitleContainingIgnoreCase(keyword);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-location")
    public ResponseEntity<List<Event>> getEventsByLocation(
            @RequestParam String location) {
        List<Event> events = eventService.findByLocationContainingIgnoreCase(location);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-description")
    public ResponseEntity<List<Event>> getEventsByDescription(
            @RequestParam String description) {
        List<Event> events = eventService.findByDescriptionContainingIgnoreCase(description);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/with-available-spots")
    public ResponseEntity<List<Event>> getEventsWithAvailableSpots() {
        List<Event> events = eventService.findEventsWithAvailableSpots();
        return ResponseEntity.ok(events);
    }
}
