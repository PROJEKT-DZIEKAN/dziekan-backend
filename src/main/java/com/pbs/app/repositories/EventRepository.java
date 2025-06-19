package com.pbs.app.repositories;


import com.pbs.app.models.Event;
import com.pbs.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);

    List<Event> findByStartTime(LocalDateTime dateTime);
    
    List<Event> findByEndTime(LocalDateTime dateTime);

    List<Event> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    List<Event> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT e FROM Event e WHERE e.maxParticipants IS NULL OR (SELECT COUNT(r) FROM EventRegistration r WHERE r.event = e) < e.maxParticipants")
    List<Event> findEventsWithAvailableSpots();
}