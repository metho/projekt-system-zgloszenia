package com.puw.systemzgloszen.service;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;

@UtilityClass
public class UserRoleUtils {
    public static boolean hasElevatedRole(UserDetails authenticatedUser) {
        return authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> {
                    String userAuthority = authority.getAuthority();
                    return userAuthority.equals("ROLE_ADMIN") || userAuthority.equals("ROLE_TICKET_MANAGER");
                });
    }
}
