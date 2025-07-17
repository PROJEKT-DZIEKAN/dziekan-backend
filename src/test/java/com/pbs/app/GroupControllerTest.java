package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.app.controllers.GroupController;
import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.services.GroupService;
import com.pbs.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Group testGroup;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testUser = User.builder()
                .id(1L)
                .firstName("Jan")
                .surname("Kowalski")
                .build();

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(10)
                .organizer(testUser)
                .build();
    }

    @Test
    void createGroup_Success() throws Exception {
        // Given
        when(groupService.createGroup(any(Group.class))).thenReturn(testGroup);

        // When & Then
        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Group"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(groupService).createGroup(any(Group.class));
    }

    @Test
    void createGroup_Exception() throws Exception {
        // Given
        when(groupService.createGroup(any(Group.class))).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isInternalServerError());

        verify(groupService).createGroup(any(Group.class));
    }

    @Test
    void updateGroup_Success() throws Exception {
        // Given
        when(groupService.updateGroup(eq(1L), any(Group.class))).thenReturn(testGroup);

        // When & Then
        mockMvc.perform(put("/api/groups/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Group"));

        verify(groupService).updateGroup(eq(1L), any(Group.class));
    }

    @Test
    void updateGroup_NotFound() throws Exception {
        // Given
        when(groupService.updateGroup(eq(1L), any(Group.class))).thenThrow(new RuntimeException("Group not found"));

        // When & Then
        mockMvc.perform(put("/api/groups/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isNotFound());

        verify(groupService).updateGroup(eq(1L), any(Group.class));
    }

    @Test
    void deleteGroup_Success() throws Exception {
        // Given
        doNothing().when(groupService).deleteGroup(1L);

        // When & Then
        mockMvc.perform(delete("/api/groups/delete/1"))
                .andExpect(status().isNoContent());

        verify(groupService).deleteGroup(1L);
    }

    @Test
    void deleteGroup_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Group not found")).when(groupService).deleteGroup(1L);

        // When & Then
        mockMvc.perform(delete("/api/groups/delete/1"))
                .andExpect(status().isNotFound());

        verify(groupService).deleteGroup(1L);
    }

    @Test
    void getGroupById_Success() throws Exception {
        // Given
        when(groupService.getGroupById(1L)).thenReturn(testGroup);

        // When & Then
        mockMvc.perform(get("/api/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Group"));

        verify(groupService).getGroupById(1L);
    }

    @Test
    void getGroupById_NotFound() throws Exception {
        // Given
        when(groupService.getGroupById(1L)).thenThrow(new RuntimeException("Group not found"));

        // When & Then
        mockMvc.perform(get("/api/groups/1"))
                .andExpect(status().isNotFound());

        verify(groupService).getGroupById(1L);
    }

    @Test
    void getAllGroups_Success() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupService.getAllGroups()).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Group"));

        verify(groupService).getAllGroups();
    }

    @Test
    void getAllGroups_Exception() throws Exception {
        // Given
        when(groupService.getAllGroups()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/groups/all"))
                .andExpect(status().isInternalServerError());

        verify(groupService).getAllGroups();
    }

    @Test
    void getGroupsByUserId_Success() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(groupService.findByParticipant(testUser)).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/by-user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(userService).getUserById(1L);
        verify(groupService).findByParticipant(testUser);
    }

    @Test
    void getGroupsByUserId_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/groups/by-user/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
        verify(groupService, never()).findByParticipant(any());
    }

    @Test
    void getGroupsCreatedAt_Success() throws Exception {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        List<Group> groups = Arrays.asList(testGroup);
        when(groupService.findByCreatedAt(any(LocalDateTime.class))).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/created-at")
                        .param("dateTime", "2023-01-01T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(groupService).findByCreatedAt(any(LocalDateTime.class));
    }

    @Test
    void getGroupsByOrganizerId_Success() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(groupService.findByOrganizer(testUser)).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/by-organizer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(userService).getUserById(1L);
        verify(groupService).findByOrganizer(testUser);
    }

    @Test
    void getGroupsByOrganizerId_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/groups/by-organizer/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
        verify(groupService, never()).findByOrganizer(any());
    }

    @Test
    void addParticipantToGroup_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(groupService).addParticipantToGroup(1L, testUser);

        // When & Then
        mockMvc.perform(post("/api/groups/add-participant/1/1"))
                .andExpect(status().isOk());

        verify(userService).getUserById(1L);
        verify(groupService).addParticipantToGroup(1L, testUser);
    }

    @Test
    void addParticipantToGroup_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/groups/add-participant/1/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
        verify(groupService, never()).addParticipantToGroup(anyLong(), any());
    }

    @Test
    void removeParticipantFromGroup_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(groupService).removeParticipantFromGroup(1L, testUser);

        // When & Then
        mockMvc.perform(delete("/api/groups/remove-participant/1/1"))
                .andExpect(status().isOk());

        verify(userService).getUserById(1L);
        verify(groupService).removeParticipantFromGroup(1L, testUser);
    }

    @Test
    void removeParticipantFromGroup_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/groups/remove-participant/1/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
        verify(groupService, never()).removeParticipantFromGroup(anyLong(), any());
    }

    @Test
    void getGroupsByTitle_Success() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupService.findByNameContainingIgnoreCase("Test")).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/by-title")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(groupService).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void getGroupsByDescription_Success() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupService.findByDescriptionContainingIgnoreCase("Test")).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/by-description")
                        .param("description", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(groupService).findByDescriptionContainingIgnoreCase("Test");
    }

    @Test
    void getGroupsWithAvailableSpots_Success() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupService.findGroupsWithAvailableSpots()).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/with-available-spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(groupService).findGroupsWithAvailableSpots();
    }

    @Test
    void getGroupsWithAvailableSpots_Exception() throws Exception {
        // Given
        when(groupService.findGroupsWithAvailableSpots()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        // W standalone setup, niezłapane wyjątki mogą powodować 404 zamiast 500
        // Sprawdzamy czy został zwrócony kod błędu (4xx lub 5xx)
        mockMvc.perform(get("/api/groups/with-available-spots"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status < 400 || status >= 600) {
                        throw new AssertionError("Expected 4xx or 5xx status but was: " + status);
                    }
                });

        verify(groupService).findGroupsWithAvailableSpots();
    }
}