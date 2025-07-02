package com.pbs.app.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data                       // generuje gettery, settery, toString, equals, hashCode
@NoArgsConstructor          // generuje konstruktor bezparametrowy
@AllArgsConstructor         // generuje konstruktor ze wszystkimi polami
public class FeedPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column
    private String title;

    @NotBlank
    @Size(max = 2000)
    @Column
    private String content;

    @NotNull
    @Column
    private LocalDateTime postedAt;
}