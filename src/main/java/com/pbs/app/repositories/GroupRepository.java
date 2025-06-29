package com.pbs.app.repositories;

import com.pbs.app.models.Event;
import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByOrganizer(User organizer);
    List<Group> findByParticipants(User user);
    List<Group> findByDescriptionContainingIgnoreCase(String description);
    List<Group> findByNameContainingIgnoreCase(String keyword);
    List<Group> findByCreatedAt(LocalDateTime createdAt);

    @Query("SELECT g FROM Group g WHERE g.maxParticipants IS NULL OR (SELECT COUNT(m) FROM g.participants m) < g.maxParticipants")
    List<Group> findGroupsWithAvailableSpots();
}
