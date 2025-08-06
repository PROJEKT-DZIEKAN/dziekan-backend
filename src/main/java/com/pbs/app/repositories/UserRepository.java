package com.pbs.app.repositories;
import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRegistrationStatus(RegistrationStatus status);
    boolean existsByUserID(String userID);
    Optional<User> findByUserID(String userID);
}
