package com.genzipher.identityservice.Config;

import com.genzipher.identityservice.Model.Permission;
import com.genzipher.identityservice.Model.RoleName;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class RolePermissionConfig {

    private static final Map<RoleName, Set<Permission>> ROLE_PERMISSIONS =
            new EnumMap<>(RoleName.class);

    static {
        // Base permissions
        Set<Permission> patient = EnumSet.of(Permission.READ);

        Set<Permission> doctor = EnumSet.of(
                Permission.READ,
                Permission.WRITE,
                Permission.UPDATE
        );

        Set<Permission> admin = EnumSet.allOf(Permission.class);
        Set<Permission> systemAdmin = EnumSet.allOf(Permission.class);
        Set<Permission> superAdmin = EnumSet.allOf(Permission.class);

        ROLE_PERMISSIONS.put(RoleName.PATIENT, patient);
        ROLE_PERMISSIONS.put(RoleName.DOCTOR, doctor);
        ROLE_PERMISSIONS.put(RoleName.ADMIN, admin);
        ROLE_PERMISSIONS.put(RoleName.SYSTEM_ADMIN, systemAdmin);
        ROLE_PERMISSIONS.put(RoleName.SUPER_ADMIN, superAdmin);
    }

    public Set<Permission> getPermissions(RoleName role) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class));
    }

}
