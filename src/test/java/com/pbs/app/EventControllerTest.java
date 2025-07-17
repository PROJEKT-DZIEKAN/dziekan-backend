package com.pbs.app;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;

import com.pbs.app.controllers.EventController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pbs.app.models.Event;
import com.pbs.app.models.User;
import com.pbs.app.services.EventServiceImpl;
import com.pbs.app.services.UserService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController Tests")
class EventControllerTest {

    @Mock
    private EventServiceImpl eventService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Event testEvent;
    private User testOrganizer;
    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testStartTime = LocalDateTime.of(2024, 12, 25, 10, 0);
        testEndTime = LocalDateTime.of(2024, 12, 25, 18, 0);

        testOrganizer = User.builder()
                .id(1L)
                .firstName("Jan")
                .surname("Kowalski")
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startTime(testStartTime)
                .endTime(testEndTime)
                .location("Test Location")
                .latitude(52.2297)
                .longitude(21.0122)
                .maxParticipants(50)
                .organizer(testOrganizer)
                .build();
    }

    @Test
    @DisplayName("POST /api/events/create - Should create event successfully")
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        
        when(eventService.createEvent(any(Event.class))).thenReturn(testEvent);

        
        mockMvc.perform(post("/api/events/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.location").value("Test Location"))
                .andExpect(jsonPath("$.maxParticipants").value(50));

        verify(eventService).createEvent(any(Event.class));
    }

    @Test
    @DisplayName("POST /api/events/create - Should handle validation errors")
    void createEvent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        Event invalidEvent = Event.builder()
                .title("") // Empty title - should fail validation
                .build();

        // When & Then
        mockMvc.perform(post("/api/events/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any(Event.class));
    }

    @Test
    @DisplayName("PUT /api/events/update/{id} - Should update event successfully")
    void updateEvent_ShouldReturnUpdatedEvent() throws Exception {
        // Given
        Event updatedEvent = Event.builder()
                .id(1L)
                .title("Updated Event")
                .description("Updated Description")
                .startTime(testStartTime)
                .endTime(testEndTime)
                .location("Updated Location")
                .build();

        when(eventService.updateEvent(eq(1L), any(Event.class))).thenReturn(updatedEvent);

        // When & Then
        mockMvc.perform(put("/api/events/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.location").value("Updated Location"));

        verify(eventService).updateEvent(eq(1L), any(Event.class));
    }

    @Test
    @DisplayName("PUT /api/events/update/{id} - Should return 404 when event not found")
    void updateEvent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        when(eventService.updateEvent(eq(999L), any(Event.class)))
                .thenThrow(new EntityNotFoundException("Event not found"));

        // When & Then
        mockMvc.perform(put("/api/events/update/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isNotFound());

        verify(eventService).updateEvent(eq(999L), any(Event.class));
    }

    @Test
    @DisplayName("DELETE /api/events/delete/{id} - Should delete event successfully")
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(eventService).deleteEvent(1L);

        // When & Then
        mockMvc.perform(delete("/api/events/delete/1"))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(1L);
    }

    @Test
    @DisplayName("DELETE /api/events/delete/{id} - Should return 404 when event not found")
    void deleteEvent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Event not found"))
                .when(eventService).deleteEvent(999L);

        // When & Then
        mockMvc.perform(delete("/api/events/delete/999"))
                .andExpect(status().isNotFound());

        verify(eventService).deleteEvent(999L);
    }

    @Test
    @DisplayName("GET /api/events/{id} - Should return event by id")
    void getEventById_ShouldReturnEvent() throws Exception {
        // Given
        when(eventService.getEventById(1L)).thenReturn(testEvent);

        // When & Then
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(eventService).getEventById(1L);
    }

    @Test
    @DisplayName("GET /api/events/{id} - Should return 404 when event not found")
    void getEventById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        when(eventService.getEventById(999L))
                .thenThrow(new EntityNotFoundException("Event not found"));

        // When & Then
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());

        verify(eventService).getEventById(999L);
    }

    @Test
    @DisplayName("GET /api/events - Should return all events")
    void getAllEvents_ShouldReturnEventList() throws Exception {
        // Given
        Event secondEvent = Event.builder()
                .id(2L)
                .title("Second Event")
                .description("Second Description")
                .startTime(testStartTime.plusDays(1))
                .endTime(testEndTime.plusDays(1))
                .location("Second Location")
                .build();

        List<Event> events = Arrays.asList(testEvent, secondEvent);
        when(eventService.getAllEvents()).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Event"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Second Event"));

        verify(eventService).getAllEvents();
    }

    @Test
    @DisplayName("GET /api/events - Should return empty list when no events")
    void getAllEvents_WhenNoEvents_ShouldReturnEmptyList() throws Exception {
        // Given
        when(eventService.getAllEvents()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(eventService).getAllEvents();
    }

    @Test
    @DisplayName("GET /api/events/between - Should return events between dates")
    void getEventsBetween_ShouldReturnEventsInRange() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        List<Event> events = Arrays.asList(testEvent);

        when(eventService.getEventsStartTimeBetween(start, end)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/between")
                .param("start", start.toString())
                .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(eventService).getEventsStartTimeBetween(start, end);
    }

    @Test
    @DisplayName("GET /api/events/by-start-time - Should return events by start time")
    void getEventsByStartTime_ShouldReturnMatchingEvents() throws Exception {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findByStartTime(testStartTime)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/by-start-time")
                .param("startTime", testStartTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(eventService).findByStartTime(testStartTime);
    }

    @Test
    @DisplayName("GET /api/events/by-end-time - Should return events by end time")
    void getEventsByEndTime_ShouldReturnMatchingEvents() throws Exception {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findByEndTime(testEndTime)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/by-end-time")
                .param("endTime", testEndTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(eventService).findByEndTime(testEndTime);
    }

    @Test
    @DisplayName("GET /api/events/upcoming - Should return upcoming events")
    void getUpcomingEvents_ShouldReturnFutureEvents() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findUpcomingEvents(any(LocalDateTime.class))).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(eventService).findUpcomingEvents(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("GET /api/events/upcoming - Should accept custom now parameter")
    void getUpcomingEvents_WithCustomNow_ShouldUseProvidedTime() throws Exception {
        // Given
        LocalDateTime customNow = LocalDateTime.of(2024, 12, 1, 10, 0);
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findUpcomingEvents(customNow)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/upcoming")
                .param("now", customNow.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(eventService).findUpcomingEvents(customNow);
    }

    @Test
    @DisplayName("GET /api/events/by-title - Should return events by title keyword")
    void getEventsByTitle_ShouldReturnMatchingEvents() throws Exception {
        // Given
        String keyword = "Test";
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findByTitleContainingIgnoreCase(keyword)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/by-title")
                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Event"));

        verify(eventService).findByTitleContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("GET /api/events/by-location - Should return events by location")
    void getEventsByLocation_ShouldReturnMatchingEvents() throws Exception {
        // Given
        String location = "Test";
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findByLocationContainingIgnoreCase(location)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/by-location")
                .param("location", location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].location").value("Test Location"));

        verify(eventService).findByLocationContainingIgnoreCase(location);
    }

    @Test
    @DisplayName("GET /api/events/by-description - Should return events by description")
    void getEventsByDescription_ShouldReturnMatchingEvents() throws Exception {
        // Given
        String description = "Test";
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findByDescriptionContainingIgnoreCase(description)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/by-description")
                .param("description", description))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        verify(eventService).findByDescriptionContainingIgnoreCase(description);
    }

    @Test
    @DisplayName("GET /api/events/by-organizer/{organizerId} - Should return events by organizer")
    void getEventsByOrganizer_ShouldReturnOrganizerEvents() throws Exception {
        // Given
        Long organizerId = 1L;
        List<Event> events = Arrays.asList(testEvent);
        when(userService.getUserById(organizerId)).thenReturn(Optional.of(testOrganizer));
        when(eventService.findByOrganizer(testOrganizer)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/by-organizer/" + organizerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(userService).getUserById(organizerId);
        verify(eventService).findByOrganizer(testOrganizer);
    }

    @Test
    @DisplayName("GET /api/events/by-organizer/{organizerId} - Should return 404 when organizer not found")
    void getEventsByOrganizer_WithNonExistentOrganizer_ShouldReturnNotFound() throws Exception {
        // Given
        Long organizerId = 999L;
        when(userService.getUserById(organizerId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/events/by-organizer/" + organizerId))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(organizerId);
        verify(eventService, never()).findByOrganizer(any(User.class));
    }

    @Test
    @DisplayName("GET /api/events/with-available-spots - Should return events with available spots")
    void getEventsWithAvailableSpots_ShouldReturnEventsWithSpots() throws Exception {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findEventsWithAvailableSpots()).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events/with-available-spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maxParticipants").value(50));

        verify(eventService).findEventsWithAvailableSpots();
    }

    @Test
    @DisplayName("GET /api/events/with-available-spots - Should return empty list when no available spots")
    void getEventsWithAvailableSpots_WhenNoAvailableSpots_ShouldReturnEmptyList() throws Exception {
        // Given
        when(eventService.findEventsWithAvailableSpots()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/events/with-available-spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(eventService).findEventsWithAvailableSpots();
    }

}