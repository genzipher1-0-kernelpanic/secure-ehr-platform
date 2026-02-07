package com.genzipher.identityservice.Service;

import com.genzipher.identityservice.Config.RolePermissionConfig;
import com.genzipher.identityservice.Model.Permission;
import com.genzipher.identityservice.Model.UserStatus;
import com.genzipher.identityservice.Repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionConfig rolePermissionConfig;

    public CustomerUserDetailsService(UserRepository userRepository,
                                      RolePermissionConfig rolePermissionConfig) {
        this.userRepository = userRepository;
        this.rolePermissionConfig = rolePermissionConfig;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        var user = userRepository.findWithRolesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Roles
        var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(user.getStatus() == UserStatus.LOCKED)
                .disabled(user.getStatus() == UserStatus.DISABLED)
                .build();
    }

}
