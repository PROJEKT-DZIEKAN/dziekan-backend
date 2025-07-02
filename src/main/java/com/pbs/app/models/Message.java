package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name="sent_at", nullable=false, updatable=false)
    private Instant sentAt;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

}
