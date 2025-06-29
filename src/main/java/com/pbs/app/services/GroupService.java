package com.pbs.app.services;

import com.pbs.app.models.Group;
import com.pbs.app.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupService {
    Group createGroup(Group group);

    Group updateGroup(Long id, Group group);

    void deleteGroup(Long id);

    Group getGroupById(Long id);

    void addParticipantToGroup(Long groupId, User participant);

    void removeParticipantFromGroup(Long groupId, User participant);

    // Group getGroupByName(String name);

    List<Group> findByCreatedAt(LocalDateTime createdAt);

    List<Group> getAllGroups();

    List<Group> findByOrganizer(User organizer);

    List<Group> findByParticipant(User participant);

    List<Group> findByNameContainingIgnoreCase(String keyword);

    List<Group> findByDescriptionContainingIgnoreCase(String description);

    List<Group> findGroupsWithAvailableSpots();
}