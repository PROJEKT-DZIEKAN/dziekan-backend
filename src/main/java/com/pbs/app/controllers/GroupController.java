package com.pbs.app.controllers;

import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.services.GroupService;
import com.pbs.app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@Valid @RequestBody Group group) {
        try {
            Group createdGroup = groupService.createGroup(group);
            return ResponseEntity.ok(createdGroup);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @Valid @RequestBody Group group) {
        try {
            Group updatedGroup = groupService.updateGroup(id, group);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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