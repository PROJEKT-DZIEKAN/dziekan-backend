package com.pbs.app.models;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventGroupId implements Serializable {
    private Long eventId;
    private Long groupId;
}