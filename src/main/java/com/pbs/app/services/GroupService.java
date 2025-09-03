package com.pbs.app.services;

import com.pbs.app.models.Event;
import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.repositories.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            return;
        }
        group.getParticipants().remove(participant);
        groupRepository.save(group);
    }

    @Transactional
    public List<String> addParticipantsToGroup(Long groupId, Set<User> participants) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        List<String> results = new ArrayList<>();

        if (group.getMaxParticipants() != null) {
            int currentSize = group.getParticipants().size();
            int requestedSize = participants.size();
            int availableSpots = group.getMaxParticipants() - currentSize;

            if (requestedSize > availableSpots) {
                throw new IllegalStateException(
                        String.format("Cannot add %d participants. Only %d spots available.",
                                requestedSize, availableSpots)
                );
            }
        }

        for (User participant : participants) {
            try {
                if (group.getParticipants().contains(participant)) {
                    results.add("User " + participant.getId() + " is already in the group");
                    continue;
                }

                group.getParticipants().add(participant);
                results.add("User " + participant.getId() + " successfully added to group");

            } catch (Exception e) {
                results.add("Failed to add user " + participant.getId() + ": " + e.getMessage());
            }
        }

        groupRepository.save(group);
        return results;
    }

    @Transactional
    public List<String> removeParticipantsFromGroup(Long groupId, Set<User> participants) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        List<String> results = new ArrayList<>();

        for (User participant : participants) {
            try {
                if (!group.getParticipants().contains(participant)) {
                    results.add("User " + participant.getId() + " is not in the group");
                    continue;
                }

                group.getParticipants().remove(participant);
                results.add("User " + participant.getId() + " successfully removed from group");

            } catch (Exception e) {
                results.add("Failed to remove user " + participant.getId() + ": " + e.getMessage());
            }
        }

        groupRepository.save(group);
        return results;
    }
    @Transactional
    public List<String> addEventsToGroup(Long groupId, Set<Event> events) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        List<String> results = new ArrayList<>();

        for (Event event : events) {
            try {
                if (group.getEvents().contains(event)) {
                    results.add("Event " + event.getId() + " is already assigned to the group");
                    continue;
                }

                group.getEvents().add(event);
                event.getGroups().add(group);
                results.add("Event " + event.getId() + " successfully added to group");

            } catch (Exception e) {
                results.add("Failed to add event " + event.getId() + ": " + e.getMessage());
            }
        }

        groupRepository.save(group);
        return results;
    }

    @Transactional
    public List<String> removeEventsFromGroup(Long groupId, Set<Event> events) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        List<String> results = new ArrayList<>();

        for (Event event : events) {
            try {
                if (!group.getEvents().contains(event)) {
                    results.add("Event " + event.getId() + " is not assigned to the group");
                    continue;
                }

                group.getEvents().remove(event);
                event.getGroups().remove(group);
                results.add("Event " + event.getId() + " successfully removed from group");

            } catch (Exception e) {
                results.add("Failed to remove event " + event.getId() + ": " + e.getMessage());
            }
        }

        groupRepository.save(group);
        return results;
    }


    @Transactional
    public void addEventToGroup(Long groupId, Event event) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        if (group.getEvents().contains(event)) {
            return;
        }

        group.getEvents().add(event);
        event.getGroups().add(group);
        groupRepository.save(group);
    }

    @Transactional
    public void removeEventFromGroup(Long groupId, Event event) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " does not exist."));

        if (!group.getEvents().contains(event)) {
            throw new EntityNotFoundException("Event with id " + event.getId() + " is not assigned to group " + groupId);
        }

        group.getEvents().remove(event);
        event.getGroups().remove(group);
        groupRepository.save(group);
    }


}
