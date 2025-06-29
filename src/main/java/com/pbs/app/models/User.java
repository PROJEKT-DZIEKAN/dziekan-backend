package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pbs.app.enums.RegistrationStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Column(name="first_name", nullable = false)
    private String FirstName;

    @Column(name="surname", nullable = false)
    private String surname;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegistrationStatus registrationStatus = RegistrationStatus.NotRegistered;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> eventRegistrations = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
}
