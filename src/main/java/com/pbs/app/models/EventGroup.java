package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventGroup {
    @EmbeddedId
    private EventGroupId id;

    @ManyToOne
    @MapsId("event")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @MapsId("group")
    @JoinColumn(name = "group_id")
    private Group group;
}