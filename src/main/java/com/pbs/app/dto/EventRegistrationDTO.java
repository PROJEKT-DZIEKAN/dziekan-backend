package com.pbs.app.dto;

import com.pbs.app.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationDTO {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String userFirstName;
    private String userSurname;
    private String userEmail;
    private LocalDateTime registeredAt;
    private RegistrationStatus status;
    private LocalDateTime statusUpdatedAt;
}
