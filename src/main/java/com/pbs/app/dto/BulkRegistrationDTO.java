package com.pbs.app.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkRegistrationDTO {

    @NotEmpty(message = "User IDs list cannot be empty")
    private List<Long> userIds;

    private String reason; // Opcjonalny powód rejestracji/anulowania
}
