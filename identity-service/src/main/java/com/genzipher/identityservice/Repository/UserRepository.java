package com.genzipher.identityservice.Repository;

import com.genzipher.identityservice.Model.RoleName;
import com.genzipher.identityservice.Model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByEmail(String email);

    @Query("""
           select count(u)
           from User u
           join u.roles r
           where r.name = :roleName
           """)
    long countUsersWithRole(RoleName roleName);

    boolean existsByEmail(String email);

}
