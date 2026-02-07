package com.genzipher.identityservice.Repository;

import com.genzipher.identityservice.Model.Role;
import com.genzipher.identityservice.Model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);


}
