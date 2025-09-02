package com.pbs.app.services;

import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.repositories.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository)
    {this.groupRepository=groupRepository;}


    @Transactional
    public Group createGroup(Group group) {
        group.setId(null); // Ensure the ID is null to use the generator
        return groupRepository.save(group);
    }

    @Transactional
    public Group updateGroup(Long id, Group group)
    {
        if (!groupRepository.existsById(id)) {
            throw new EntityNotFoundException("Group with id " + id + " does not exist.");
        }
        group.setId(id); // Set the ID to ensure the correct entity is updated
        return groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new EntityNotFoundException("Group with id " + id + " does not exist.");
        }
        groupRepository.deleteById(id);
    }

    @Transactional
    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + id + " does not exist."));
    }

    @Transactional
    public List<Group> findByCreatedAt(LocalDateTime createdAt) {
        List<Group> groups = groupRepository.findByCreatedAt(createdAt);
        if (groups.isEmpty()) {
            throw new EntityNotFoundException("Group with createdAt " + createdAt + " does not exist.");
        }
        return groups;
    }

    @Transactional
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }


    @Transactional
    public List<Group> findByNameContainingIgnoreCase(String name) {
        return groupRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public List<Group> findByDescriptionContainingIgnoreCase(String description) {
        return groupRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Transactional
    public List<Group> findGroupsWithAvailableSpots() {
        return groupRepository.findGroupsWithAvailableSpots();
    }

    @Transactional
    public void addParticipantToGroup(Long groupId, User participant) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));
        if (group.getParticipants().contains(participant)) {
            return; // Użytkownik już jest w grupie
        }
        if (group.getMaxParticipants() != null && group.getParticipants().size() >= group.getMaxParticipants()) {
            throw new IllegalStateException("Group is full.");
        }
        group.getParticipants().add(participant);
        groupRepository.save(group);
    }


    @Transactional
    public void removeParticipantFromGroup(Long groupId, User participant) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));
        if (!group.getParticipants().contains(participant)) {
            return; // Użytkownik nie jest w grupie
        }
        group.getParticipants().remove(participant);
        groupRepository.save(group);
    }

}
