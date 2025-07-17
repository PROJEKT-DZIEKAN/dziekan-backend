package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.Event;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.EventRepository;
import com.pbs.app.repositories.NotificationRepository;
import com.pbs.app.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ORGANIZER")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private ObjectMapper objectMapper;
    private User testOrganizer;
    private Event testEvent;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        chatRepository.deleteAll();
        notificationRepository.deleteAll(); // jeśli istnieje
        eventRepository.deleteAll();
        userRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();
        setupTestData();
    }

    private void setupTestData() {
        testOrganizer = User.builder()
                .firstName("Jan")
                .surname("Kowalski")
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();
        testOrganizer = userRepository.save(testOrganizer);

        testEvent = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Location")
                .latitude(52.2297)
                .longitude(21.0122)
                .maxParticipants(50)
                .organizer(testOrganizer)
                .build();
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        Event newEvent = Event.builder()
                .title("New Event")
                .description("New Description")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .location("New Location")
                .latitude(50.0647)
                .longitude(19.9450)
                .maxParticipants(100)
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Event"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.location").value("New Location"))
                .andExpect(jsonPath("$.latitude").value(50.0647))
                .andExpect(jsonPath("$.longitude").value(19.9450))
                .andExpect(jsonPath("$.maxParticipants").value(100))
                .andExpect(jsonPath("$.id").exists());

        List<Event> events = eventRepository.findAll();
        assertEquals(2, events.size());
    }

    @Test
    void createEvent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Event invalidEvent = Event.builder()
                .title("") // Empty title - should fail validation
                .description("Valid Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Valid Location")
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() throws Exception {
        Event updatedEvent = Event.builder()
                .title("Updated Event")
                .description("Updated Description")
                .startTime(LocalDateTime.now().plusDays(3))
                .endTime(LocalDateTime.now().plusDays(3).plusHours(4))
                .location("Updated Location")
                .latitude(51.1079)
                .longitude(17.0385)
                .maxParticipants(75)
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(put("/api/events/update/{id}", testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.location").value("Updated Location"))
                .andExpect(jsonPath("$.latitude").value(51.1079))
                .andExpect(jsonPath("$.longitude").value(17.0385))
                .andExpect(jsonPath("$.maxParticipants").value(75))
                .andExpect(jsonPath("$.id").value(testEvent.getId()));
    }

    @Test
    void updateEvent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Event updatedEvent = Event.builder()
                .title("Updated Event")
                .description("Updated Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Updated Location")
                .build();

        mockMvc.perform(put("/api/events/update/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/events/delete/{id}", testEvent.getId()))
                .andExpect(status().isNoContent());

        assertFalse(eventRepository.existsById(testEvent.getId()));
    }

    @Test
    void deleteEvent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/events/delete/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventById_ShouldReturnEvent() throws Exception {
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.location").value("Test Location"))
                .andExpect(jsonPath("$.latitude").value(52.2297))
                .andExpect(jsonPath("$.longitude").value(21.0122))
                .andExpect(jsonPath("$.maxParticipants").value(50));
    }

    @Test
    void getEventById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/events/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents() throws Exception {
        // Create additional test event
        Event secondEvent = Event.builder()
                .title("Second Event")
                .description("Second Description")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .location("Second Location")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(secondEvent);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Test Event", "Second Event")))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("Test Description", "Second Description")))
                .andExpect(jsonPath("$[*].location", containsInAnyOrder("Test Location", "Second Location")));
    }

    @Test
    void getEventsBetween_ShouldReturnEventsInDateRange() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        // Create events within and outside the range
        Event eventInRange = Event.builder()
                .title("Event In Range")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(eventInRange);

        Event eventOutOfRange = Event.builder()
                .title("Event Out Of Range")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(5))
                .endTime(LocalDateTime.now().plusDays(5).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(eventOutOfRange);

        mockMvc.perform(get("/api/events/between")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // testEvent and eventInRange
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Test Event", "Event In Range")));
    }

    @Test
    void getEventsByStartTime_ShouldReturnEventsWithExactStartTime() throws Exception {
        LocalDateTime specificTime = LocalDateTime.now().plusDays(10).withNano(0); // Zeruj nanosekundy

        Event eventWithSpecificTime = Event.builder()
                .title("Specific Time Event")
                .description("Description")
                .startTime(specificTime)
                .endTime(specificTime.plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();

        eventRepository.saveAndFlush(eventWithSpecificTime); // Wymuś natychmiastowy zapis

        mockMvc.perform(get("/api/events/by-start-time")
                        .param("startTime", specificTime.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Specific Time Event"));
    }

    @Test
    void getEventsByEndTime_ShouldReturnEventsWithExactEndTime() throws Exception {
        // 1. Ustaw czas z zerowanymi nanosekundami
        LocalDateTime specificEndTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0); // Zerowanie nanosekund jest kluczowe!

        // 2. Zapisz organizatora z wymuszeniem flush
        User organizer = userRepository.saveAndFlush(testOrganizer);

        // 3. Stwórz i zapisz wydarzenie
        Event event = Event.builder()
                .title("Specific End Time Event")
                .description("Description")
                .startTime(specificEndTime.minusHours(2))
                .endTime(specificEndTime) // Używamy dokładnie tego samego czasu
                .location("Location")
                .organizer(organizer)
                .build();

        eventRepository.saveAndFlush(event); // Wymuszamy natychmiastowy zapis

        // 4. Wykonaj zapytanie z tym samym czasem
        mockMvc.perform(get("/api/events/by-end-time")
                        .param("endTime", specificEndTime.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Specific End Time Event"));
    }

    @Test
    void getUpcomingEvents_ShouldReturnFutureEvents() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // Create past event
        Event pastEvent = Event.builder()
                .title("Past Event")
                .description("Description")
                .startTime(now.minusDays(1))
                .endTime(now.minusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(pastEvent);

        mockMvc.perform(get("/api/events/upcoming")
                        .param("now", now.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void getUpcomingEvents_WithoutNowParameter_ShouldUseCurrentTime() throws Exception {
        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void getEventsByTitle_ShouldReturnEventsContainingKeyword() throws Exception {
        Event eventWithDifferentTitle = Event.builder()
                .title("Workshop on Testing")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(eventWithDifferentTitle);

        mockMvc.perform(get("/api/events/by-title")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Test Event", "Workshop on Testing")));
    }

    @Test
    void getEventsByTitle_CaseInsensitive_ShouldReturnEvents() throws Exception {
        mockMvc.perform(get("/api/events/by-title")
                        .param("keyword", "TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void getEventsByLocation_ShouldReturnEventsContainingLocation() throws Exception {
        Event eventWithDifferentLocation = Event.builder()
                .title("Event Title")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Hall")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(eventWithDifferentLocation);

        mockMvc.perform(get("/api/events/by-location")
                        .param("location", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].location", containsInAnyOrder("Test Location", "Test Hall")));
    }

    @Test
    void getEventsByDescription_ShouldReturnEventsContainingDescription() throws Exception {
        Event eventWithDifferentDescription = Event.builder()
                .title("Event Title")
                .description("Testing workshop description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();
        eventRepository.save(eventWithDifferentDescription);

        mockMvc.perform(get("/api/events/by-description")
                        .param("description", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("Test Description", "Testing workshop description")));
    }

    @Test
    void getEventsByOrganizer_ShouldReturnEventsForSpecificOrganizer() throws Exception {
        User anotherOrganizer = User.builder()
                .firstName("Anna")
                .surname("Nowak")
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();
        anotherOrganizer = userRepository.save(anotherOrganizer);

        Event eventByAnotherOrganizer = Event.builder()
                .title("Another Event")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(anotherOrganizer)
                .build();
        eventRepository.save(eventByAnotherOrganizer);

        mockMvc.perform(get("/api/events/by-organizer/{organizerId}", testOrganizer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void getEventsByOrganizer_WithNonExistentOrganizer_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/events/by-organizer/{organizerId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventsWithAvailableSpots_ShouldReturnEventsWithSpaces() throws Exception {
        // Create event with no max participants (unlimited)
        Event unlimitedEvent = Event.builder()
                .title("Unlimited Event")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .maxParticipants(null)
                .organizer(testOrganizer)
                .build();
        eventRepository.save(unlimitedEvent);

        // Create event with limited participants but not full
        Event limitedEvent = Event.builder()
                .title("Limited Event")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .maxParticipants(5)
                .organizer(testOrganizer)
                .build();
        eventRepository.save(limitedEvent);

        mockMvc.perform(get("/api/events/with-available-spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // testEvent, unlimitedEvent, limitedEvent
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Test Event", "Unlimited Event", "Limited Event")));
    }

    @Test
    void createEvent_WithNullMaxParticipants_ShouldWork() throws Exception {
        Event eventWithNullMax = Event.builder()
                .title("Null Max Event")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .maxParticipants(null)
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithNullMax)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Null Max Event"))
                .andExpect(jsonPath("$.maxParticipants").isEmpty());
    }

    @Test
    void createEvent_WithNegativeMaxParticipants_ShouldReturnBadRequest() throws Exception {
        Event eventWithNegativeMax = Event.builder()
                .title("Negative Max Event")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .maxParticipants(-1)
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithNegativeMax)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithTooLongTitle_ShouldReturnBadRequest() throws Exception {
        String longTitle = "a".repeat(256); // Exceeds 255 character limit

        Event eventWithLongTitle = Event.builder()
                .title(longTitle)
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithLongTitle)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithTooLongDescription_ShouldReturnBadRequest() throws Exception {
        String longDescription = "a".repeat(2001); // Exceeds 2000 character limit

        Event eventWithLongDescription = Event.builder()
                .title("Title")
                .description(longDescription)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithLongDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithNullStartTime_ShouldReturnBadRequest() throws Exception {
        Event eventWithNullStartTime = Event.builder()
                .title("Title")
                .description("Description")
                .startTime(null)
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Location")
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithNullStartTime)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithNullEndTime_ShouldReturnBadRequest() throws Exception {
        Event eventWithNullEndTime = Event.builder()
                .title("Title")
                .description("Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(null)
                .location("Location")
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithNullEndTime)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void integrationTest_CompleteEventLifecycle() throws Exception {
        // 1. Create event
        Event newEvent = Event.builder()
                .title("Lifecycle Event")
                .description("Lifecycle Description")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Lifecycle Location")
                .maxParticipants(10)
                .organizer(testOrganizer)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isOk())
                .andReturn();

        Event createdEvent = objectMapper.readValue(createResult.getResponse().getContentAsString(), Event.class);
        Long eventId = createdEvent.getId();

        // 2. Retrieve event by ID
        mockMvc.perform(get("/api/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lifecycle Event"));

        // 3. Update event
        Event updatedEvent = Event.builder()
                .title("Updated Lifecycle Event")
                .description("Updated Description")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .location("Updated Location")
                .maxParticipants(20)
                .organizer(testOrganizer)
                .build();

        mockMvc.perform(put("/api/events/update/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Lifecycle Event"))
                .andExpect(jsonPath("$.maxParticipants").value(20));

        // 4. Verify event appears in search results
        mockMvc.perform(get("/api/events/by-title")
                        .param("keyword", "Updated Lifecycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Updated Lifecycle Event"));

        // 5. Delete event
        mockMvc.perform(delete("/api/events/delete/{id}", eventId))
                .andExpect(status().isNoContent());

        // 6. Verify event is deleted
        mockMvc.perform(get("/api/events/{id}", eventId))
                .andExpect(status().isNotFound());
    }
}