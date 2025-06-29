package com.pbs.app.controllers;

import com.pbs.app.models.EventGroup;
import com.pbs.app.services.EventGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-groups")
@RequiredArgsConstructor
public class EventGroupController {
    private final EventGroupService eventGroupService;

    @PostMapping("/create")
    public ResponseEntity<EventGroup> createEventGroup(@Valid @RequestBody EventGroup eventGroup) {
        EventGroup createdEventGroup = eventGroupService.createEventGroup(eventGroup);
        return ResponseEntity.ok(createdEventGroup);
    }

    @PostMapping("/update")
    public ResponseEntity<EventGroup> updateEventGroup(@Valid @RequestBody EventGroup eventGroup) {
        EventGroup updatedEventGroup = eventGroupService.updateEventGroup(eventGroup);
        return ResponseEntity.ok(updatedEventGroup);
    }

//    @PostMapping("/delete")
//    public ResponseEntity<Void> deleteEventGroup(@Valid @RequestBody EventGroup eventGroup) {
//        try {
//            eventGroupService.delete(eventGroup.getEventId(), eventGroup.getGroupId());
//            return ResponseEntity.noContent().build();
//        } catch (Exception e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

}
