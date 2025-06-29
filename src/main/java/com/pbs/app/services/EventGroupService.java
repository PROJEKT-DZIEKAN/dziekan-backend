package com.pbs.app.services;

import com.pbs.app.models.EventGroup;

import java.util.List;

public interface EventGroupService {
    List<EventGroup> findAll();
    List<EventGroup> findByEventId(Long eventId);
    List<EventGroup> findByGroupId(Long groupId);
    EventGroup createEventGroup(EventGroup eventGroup);
    EventGroup updateEventGroup(EventGroup eventGroup);
    void delete(Long eventId, Long groupId);
    EventGroup findById(Long eventId, Long groupId);
    List<EventGroup> getGroupsForEvent(Long eventId);
    List<EventGroup> getEventsForGroup(Long groupId);
 }
