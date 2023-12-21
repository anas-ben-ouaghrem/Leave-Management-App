package com.telnet.leaveapp.telnetleavemanager.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {
    USER(Collections.emptySet()),
    ADMIN(
            Set.of(
                    Permission.ADMIN_READ,
                    Permission.ADMIN_CREATE,
                    Permission.ADMIN_DELETE,
                    Permission.ADMIN_UPDATE,
                    Permission.MANAGER_READ,
                    Permission.MANAGER_CREATE,
                    Permission.MANAGER_DELETE,
                    Permission.MANAGER_UPDATE
            )
    ),
    MANAGER(
            Set.of(
                    Permission.MANAGER_READ,
                    Permission.MANAGER_CREATE,
                    Permission.MANAGER_DELETE,
                    Permission.MANAGER_UPDATE
            )
    )

    ;

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
