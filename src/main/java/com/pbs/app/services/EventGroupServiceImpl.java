package com.pbs.app.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.pbs.app.models.EventGroup;
import com.pbs.app.models.EventGroupId;
import com.pbs.app.repositories.EventGroupRepository;

import java.util.List;

@Service
public class EventGroupServiceImpl implements EventGroupService {

    private final EventGroupRepository eventGroupRepository;

    public EventGroupServiceImpl(EventGroupRepository eventGroupRepository) {
        this.eventGroupRepository = eventGroupRepository;
    }

    @Override
    @Transactional
    public List<EventGroup> findAll() {
        return eventGroupRepository.findAll();
    }

    @Override
    @Transactional
    public List<EventGroup> findByEventId(Long eventId) {
        return eventGroupRepository.findByEventId(eventId);
    }

    @Override
    @Transactional
    public List<EventGroup> findByGroupId(Long groupId) {
        return eventGroupRepository.findByGroupId(groupId);
    }

    @Override
    @Transactional
    public EventGroup createEventGroup(EventGroup eventGroup) {
        return eventGroupRepository.save(eventGroup);
    }

    @Override
    @Transactional
    public EventGroup updateEventGroup(EventGroup eventGroup) {
        return eventGroupRepository.save(eventGroup);
    }

    @Override
    @Transactional
    public void delete(Long eventId, Long groupId) {
        eventGroupRepository.deleteById(new EventGroupId(eventId, groupId));
    }

    @Override
    @Transactional
    public EventGroup findById(Long eventId, Long groupId) {
        return eventGroupRepository.findById(new EventGroupId(eventId, groupId)).orElse(null);
    }

    @Override
    @Transactional
    public List<EventGroup> getGroupsForEvent(Long eventId) {
        return findByEventId(eventId);
    }

    @Override
    @Transactional
    public List<EventGroup> getEventsForGroup(Long groupId) {
        return findByGroupId(groupId);
    }
}