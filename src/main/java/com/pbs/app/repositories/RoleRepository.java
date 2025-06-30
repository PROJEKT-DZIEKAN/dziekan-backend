package com.pbs.app.repositories;

import com.pbs.app.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.pbs.app.models.User;
import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<List<User>> findUsersByRoleName(String roleName);
}
