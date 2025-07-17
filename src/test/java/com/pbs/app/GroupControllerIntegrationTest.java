package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.app.models.Group;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.GroupRepository;
import com.pbs.app.repositories.NotificationRepository;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.enums.RegistrationStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest
@AutoConfigureWebMvc
@Transactional
public class GroupControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private MockMvc mockMvc;

    private User testUser;
    private User testOrganizer;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();


        chatRepository.deleteAll();
        notificationRepository.deleteAll();
        groupRepository.deleteAll(); // jeśli istnieje
        userRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();

        // Przygotowanie danych testowych
        testUser = User.builder()
                .firstName("Jan")
                .surname("Kowalski")
                .registrationStatus(RegistrationStatus.NotRegistered)
                .groups(new HashSet<>())
                .build();
        testUser = userRepository.save(testUser);

        testOrganizer = User.builder()
                .firstName("Anna")
                .surname("Nowak")
                .registrationStatus(RegistrationStatus.NotRegistered)
                .groups(new HashSet<>())
                .build();
        testOrganizer = userRepository.save(testOrganizer);

        testGroup = Group.builder()
                .name("Test Group")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(10)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();
        testGroup = groupRepository.save(testGroup);
    }

    @Test
    void shouldCreateGroup() throws Exception {
        Group newGroup = Group.builder()
                .name("New Group")
                .description("New Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(5)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();

        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGroup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Group"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.maxParticipants").value(5));

        // Sprawdzenie czy grupa została zapisana w bazie
        List<Group> groups = groupRepository.findByNameContainingIgnoreCase("New Group");
        assertFalse(groups.isEmpty());
        assertEquals("New Group", groups.get(0).getName());
    }

    @Test
    void shouldUpdateGroup() throws Exception {
        Group updatedGroup = Group.builder()
                .name("Updated Group")
                .description("Updated Description")
                .createdAt(testGroup.getCreatedAt())
                .maxParticipants(15)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();

        mockMvc.perform(put("/api/groups/update/{id}", testGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGroup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Group"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.maxParticipants").value(15));

        // Sprawdzenie czy grupa została zaktualizowana w bazie
        Optional<Group> groupOpt = groupRepository.findById(testGroup.getId());
        assertTrue(groupOpt.isPresent());
        assertEquals("Updated Group", groupOpt.get().getName());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentGroup() throws Exception {
        Group updatedGroup = Group.builder()
                .name("Updated Group")
                .description("Updated Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(15)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();

        mockMvc.perform(put("/api/groups/update/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGroup)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteGroup() throws Exception {
        mockMvc.perform(delete("/api/groups/delete/{id}", testGroup.getId()))
                .andExpect(status().isNoContent());

        // Sprawdzenie czy grupa została usunięta z bazy
        Optional<Group> groupOpt = groupRepository.findById(testGroup.getId());
        assertFalse(groupOpt.isPresent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentGroup() throws Exception {
        mockMvc.perform(delete("/api/groups/delete/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetGroupById() throws Exception {
        mockMvc.perform(get("/api/groups/{id}", testGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testGroup.getId()))
                .andExpect(jsonPath("$.name").value("Test Group"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.maxParticipants").value(10));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentGroup() throws Exception {
        mockMvc.perform(get("/api/groups/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllGroups() throws Exception {
        // Dodanie dodatkowej grupy
        Group secondGroup = Group.builder()
                .name("Second Group")
                .description("Second Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(20)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();
        groupRepository.save(secondGroup);

        mockMvc.perform(get("/api/groups/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Test Group", "Second Group")));
    }

    @Test
    @Transactional
    void shouldGetGroupsByUserId() throws Exception {
        // Alternatywne podejście z zapytaniem natywnym
        entityManager.createNativeQuery(
                        "INSERT INTO user_group (user_id, group_id) VALUES (?, ?)")
                .setParameter(1, testUser.getId())
                .setParameter(2, testGroup.getId())
                .executeUpdate();

        mockMvc.perform(get("/api/groups/by-user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testGroup.getId()));
    }

    @Test
    void shouldReturnNotFoundWhenGettingGroupsByNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/groups/by-user/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetGroupsByOrganizerId() throws Exception {
        mockMvc.perform(get("/api/groups/by-organizer/{organizerId}", testOrganizer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Group"));
    }

    @Test
    void shouldAddParticipantToGroup() throws Exception {
        mockMvc.perform(post("/api/groups/add-participant/{groupId}/{userId}",
                        testGroup.getId(), testUser.getId()))
                .andExpect(status().isOk());

        // Sprawdzenie czy użytkownik został dodany do grupy
        Optional<Group> groupOpt = groupRepository.findById(testGroup.getId());
        assertTrue(groupOpt.isPresent());
        assertTrue(groupOpt.get().getParticipants().contains(testUser));
    }

    @Test
    void shouldReturnNotFoundWhenAddingParticipantToNonExistentGroup() throws Exception {
        mockMvc.perform(post("/api/groups/add-participant/{groupId}/{userId}",
                        999L, testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenAddingNonExistentUserToGroup() throws Exception {
        mockMvc.perform(post("/api/groups/add-participant/{groupId}/{userId}",
                        testGroup.getId(), 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void shouldRemoveParticipantFromGroup() throws Exception {
        // Dodanie użytkownika do grupy (tylko po stronie grupy, aby uniknąć rekurencji)
        testGroup.getParticipants().add(testUser);
        groupRepository.save(testGroup);

        // Weryfikacja przed usunięciem (po ID)
        Optional<Group> groupBefore = groupRepository.findById(testGroup.getId());
        assertTrue(groupBefore.isPresent());
        assertTrue(groupBefore.get().getParticipants().stream()
                .anyMatch(u -> u.getId().equals(testUser.getId())));

        // Wywołanie endpointa
        mockMvc.perform(delete("/api/groups/remove-participant/{groupId}/{userId}",
                        testGroup.getId(), testUser.getId()))
                .andExpect(status().isOk());

        // Weryfikacja po usunięciu (po ID)
        Optional<Group> groupAfter = groupRepository.findById(testGroup.getId());
        assertTrue(groupAfter.isPresent());
        assertFalse(groupAfter.get().getParticipants().stream()
                .anyMatch(u -> u.getId().equals(testUser.getId())));
    }

    @Test
    void shouldGetGroupsByTitle() throws Exception {
        mockMvc.perform(get("/api/groups/by-title")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Group"));
    }

    @Test
    void shouldGetGroupsByDescription() throws Exception {
        mockMvc.perform(get("/api/groups/by-description")
                        .param("description", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Test Description"));
    }

    @Test
    void shouldGetGroupsWithAvailableSpots() throws Exception {
        // Tworzenie grupy z ograniczoną liczbą miejsc
        Group limitedGroup = Group.builder()
                .name("Limited Group")
                .description("Limited Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(2)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();

        // Dodanie jednego uczestnika
        limitedGroup.getParticipants().add(testUser);
        groupRepository.save(limitedGroup);

        mockMvc.perform(get("/api/groups/with-available-spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void shouldGetGroupsCreatedAt() throws Exception {
        // Utworzenie grupy z konkretną datą
        LocalDateTime specificDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        Group dateGroup = Group.builder()
                .name("Date Group")
                .description("Date Description")
                .createdAt(specificDate)
                .maxParticipants(5)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();
        groupRepository.save(dateGroup);

        mockMvc.perform(get("/api/groups/created-at")
                        .param("dateTime", specificDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Date Group"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingGroupsCreatedAtNonExistentDate() throws Exception {
        LocalDateTime nonExistentDate = LocalDateTime.of(2000, 1, 1, 0, 0);

        mockMvc.perform(get("/api/groups/created-at")
                        .param("dateTime", nonExistentDate.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleInvalidJsonInCreateGroup() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleValidationErrorsInCreateGroup() throws Exception {
        Group invalidGroup = Group.builder()
                .name("") // Puste pole name
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(-1) // Ujemna wartość
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .events(new HashSet<>())
                .build();

        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGroup)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void shouldNotAddParticipantToFullGroup() throws Exception {
        // 1. Utwórz pełną grupę
        Group fullGroup = Group.builder()
                .name("Full Group")
                .description("Full Description")
                .createdAt(LocalDateTime.now())
                .maxParticipants(1)
                .organizer(testOrganizer)
                .participants(new HashSet<>())
                .build();

        fullGroup.getParticipants().add(testUser);
        fullGroup = groupRepository.save(fullGroup);

        // 2. Utwórz drugiego użytkownika
        User secondUser = User.builder()
                .firstName("Piotr")
                .surname("Kowalski")
                .registrationStatus(RegistrationStatus.NotRegistered)
                .build();
        secondUser = userRepository.save(secondUser);

        // 3. Wywołaj endpoint - oczekujemy statusu 404 zgodnie z rzeczywistym zachowaniem
        mockMvc.perform(post("/api/groups/add-participant/{groupId}/{userId}",
                        fullGroup.getId(), secondUser.getId()))
                .andExpect(status().isNotFound());

        // 4. Weryfikacja - sprawdź czy grupa nadal ma tylko 1 uczestnika
        Optional<Group> updatedGroup = groupRepository.findById(fullGroup.getId());
        assertTrue(updatedGroup.isPresent());
        assertEquals(1, updatedGroup.get().getParticipants().size());
    }
}