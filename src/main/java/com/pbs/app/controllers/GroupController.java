package com.pbs.app.controllers;

import com.pbs.app.models.Event;
import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.services.EventService;
import com.pbs.app.services.GroupService;
import com.pbs.app.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
            Group createdGroup = groupService.createGroup(group);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @Valid @RequestBody Group group) {
        Group updatedGroup = groupService.updateGroup(id, group);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        try {
            Group group = groupService.getGroupById(id);
            return ResponseEntity.ok(group);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Group>> getAllGroups() {
        try {
            List<Group> groups = groupService.getAllGroups();

            if (groups == null) {
                groups = new ArrayList<>();
            }
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/created-at")
    public ResponseEntity<List<Group>> getGroupsCreatedAt(@RequestParam LocalDateTime dateTime) {
        try {
            List<Group> groups = groupService.findByCreatedAt(dateTime);
            return ResponseEntity.ok(groups);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/add-participant/{groupId}/{userId}")
    public ResponseEntity<Void> addParticipantToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            groupService.addParticipantToGroup(groupId, user);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add-participants/{groupId}")
    public ResponseEntity<List<String>> addParticipantsToGroup(@PathVariable Long groupId, @RequestBody Set<User> participants) {
        try {
            List<String> results = groupService.addParticipantsToGroup(groupId, participants);
            return ResponseEntity.ok(results);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/remove-participants/{groupId}")
    public ResponseEntity<List<String>> removeParticipantsFromGroup(@PathVariable Long groupId, @RequestBody Set<User> participants) {
        try {
            List<String> results = groupService.removeParticipantsFromGroup(groupId, participants);
            return ResponseEntity.ok(results);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add-events/{groupId}")
    public ResponseEntity<List<String>> addEventsToGroup(@PathVariable Long groupId, @RequestBody Set<Event> events) {
        try {
            List<String> results = groupService.addEventsToGroup(groupId, events);
            return ResponseEntity.ok(results);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/remove-events/{groupId}")
    public ResponseEntity<List<String>> removeEventsFromGroup(@PathVariable Long groupId, @RequestBody Set<Event> events) {
        try {
            List<String> results = groupService.removeEventsFromGroup(groupId, events);
            return ResponseEntity.ok(results);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add-event/{groupId}/{eventId}")
    public ResponseEntity<Void> addEventToGroup(@PathVariable Long groupId, @PathVariable Long eventId) {
        try {
            Event event = eventService.getEventById(eventId);
            groupService.addEventToGroup(groupId, event);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/remove-event/{groupId}/{eventId}")
    public ResponseEntity<Void> removeEventFromGroup(@PathVariable Long groupId, @PathVariable Long eventId) {
        try {
            Event event = eventService.getEventById(eventId);
            groupService.removeEventFromGroup(groupId, event);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/remove-participant/{groupId}/{userId}")
    public ResponseEntity<Void> removeParticipantFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            groupService.removeParticipantFromGroup(groupId, user);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-title")
    public ResponseEntity<List<Group>> getGroupsByTitle(@RequestParam String title) {
        try {
            List<Group> groups = groupService.findByNameContainingIgnoreCase(title);
            return ResponseEntity.ok(groups);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-description")
    public ResponseEntity<List<Group>> getGroupsByDescription(@RequestParam String description) {
        try {
            List<Group> groups = groupService.findByDescriptionContainingIgnoreCase(description);
            return ResponseEntity.ok(groups);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/with-available-spots")
    public ResponseEntity<List<Group>> getGroupsWithAvailableSpots() {
        try {
            List<Group> groups = groupService.findGroupsWithAvailableSpots();
            return ResponseEntity.ok(groups);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}