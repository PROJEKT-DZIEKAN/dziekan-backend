package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.*;
import com.pbs.app.enums.RegistrationStatus;
import org.hibernate.annotations.ColumnDefault;

import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"eventRegistrations", "groups", "roles"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "surname", nullable = false)
    private String surname;

    // Nowe pola zgodne z dokumentacją mgr Kątka
    @Column(name = "position")
    private String position;

    @Column(name = "university")
    private String university;

    @Column(name = "department")
    private String department;

    @Column(name = "email")
    private String email;

    @Column(name = "user_id", unique = true)
    private String userID;

    @Column(name = "password")
    private String password;

    @Column(name = "photo_path")
    private String photoPath; // Ścieżka do zdjęcia

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    @ColumnDefault("'NotRegistered'")
    private RegistrationStatus registrationStatus = RegistrationStatus.NotRegistered;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> eventRegistrations = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "user_group",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyUserAnswer> surveyUserAnswers = new ArrayList<>();
}
