package com.pbs.app.repositories;


import com.pbs.app.models.Event;
import com.pbs.app.models.EventGroup;
import com.pbs.app.models.EventGroupId;
import com.pbs.app.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventGroupRepository extends JpaRepository<EventGroup, EventGroupId> {
    List<EventGroup> findByEventId (Long eventId);
    List<EventGroup> findByGroupId (Long groupId);
}
