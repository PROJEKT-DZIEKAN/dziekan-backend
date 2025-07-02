package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats", uniqueConstraints = @UniqueConstraint(columnNames = {"user_a_id", "user_b_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Builder.Default
    @Column(name="sent_at", nullable=false, updatable=false)
    private Instant sentAt = Instant.now();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
}
