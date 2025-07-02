package com.pbs.app.controllers;

import com.pbs.app.models.EventRegistration;
import com.pbs.app.services.EventRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.pbs.app.enums.RegistrationStatus;


@RestController
@RequestMapping("/api/event-registrations")
@RequiredArgsConstructor
public class EventRegistrationController {
    private final EventRegistrationService eventRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<EventRegistration>  createRegistration(@Valid @RequestBody EventRegistration eventRegistration) {
        EventRegistration createdRegistration = eventRegistrationService.createRegistration(eventRegistration);
        return ResponseEntity.ok(createdRegistration);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteRegistration(@RequestParam Long id) {
        try {
            eventRegistrationService.deleteRegistration(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<EventRegistration> updateRegistrationStatus(@PathVariable Long id, @RequestParam RegistrationStatus status) {
        try {
            eventRegistrationService.updateRegistrationStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<EventRegistration> updateRegistration(@PathVariable Long id, @Valid @RequestBody EventRegistration eventRegistration) {
        try {
            EventRegistration updatedRegistration = eventRegistrationService.updateRegistration(id, eventRegistration);
            return ResponseEntity.ok(updatedRegistration);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/registrations-by-event/{eventId}")
    public ResponseEntity<List<EventRegistration>> getRegistrationsByEventId(@PathVariable Long eventId) {
        List<EventRegistration> registrations = eventRegistrationService.getRegistrationsByEventId(eventId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/registrations-by-participant/{userId}")
    public ResponseEntity<List<EventRegistration>> getRegistrationsByParticipantId(@PathVariable Long userId) {
        List<EventRegistration> registrations = eventRegistrationService.getRegistrationsByParticipantId(userId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/registrations-by-status")
    public ResponseEntity<List<EventRegistration>> getRegistrationsByStatus(@RequestParam RegistrationStatus status) {
        List<EventRegistration> registrations = eventRegistrationService.getRegistrationsByStatus(status);
        return ResponseEntity.ok(registrations);
    }

//    @GetMapping("/registration-by-event-and-participant")
//    public ResponseEntity<EventRegistration> getRegistrationByEventAndParticipant(@RequestParam Long eventId, @RequestParam Long userId) {
//        return eventRegistrationService.getRegistrationByEventAndParticipant(eventId, userId)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/registration/{id}")
//    public ResponseEntity<EventRegistration> getRegistrationById(@PathVariable Long id) {
//        return eventRegistrationService.getRegistrationById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

    // work in progress kurwa mac
}

