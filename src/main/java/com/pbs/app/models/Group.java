package com.pbs.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"group\"")
public class Group {
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    @Size(max = 255)
    private String name;

    @Column
    @Size(max = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Min(0)
    @Column
    private Integer maxParticipants;

    @Schema(hidden = true)
    @ManyToOne
    @JoinColumn(nullable = true)
    private User organizer;

    @Schema(hidden = true)
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> participants = new ArrayList<>();
}