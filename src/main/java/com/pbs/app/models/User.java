package com.pbs.app.models;
import jakarta.persistence.*;
import lombok.*;
import com.pbs.app.enums.RegistrationStatus;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Column(name="first_name", nullable = false)
    private String firstName;

    @Column(name="surname", nullable = false)
    private String surname;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
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
}
