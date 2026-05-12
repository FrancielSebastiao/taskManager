package com.fransebastiao.taskmanager.util;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

public class RoleUtils {
    private static final Set<String> PRIVILEGED_ROLES = Set.of(
        "ROLE_SYSADMIN", 
        "ROLE_ADMIN",
        "ROLE_GESTOR",
        "ROLE_SUPERVISOR"
    );

    public static boolean isPrivileged(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(PRIVILEGED_ROLES::contains);
    }
}
