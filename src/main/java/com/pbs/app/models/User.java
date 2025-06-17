package com.pbs.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pbs.app.enums.RegistrationStatus;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="first_name", nullable = false)
    private String FirstName;

    @Column(name="surname", nullable = false)
    private String surname;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegistrationStatus registrationStatus = RegistrationStatus.NotRegistered;




}
