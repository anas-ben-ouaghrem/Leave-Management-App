package com.telnet.leaveapp.telnetleavemanager.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private LocalDateTime createdAt;
    private String unitName;
    private Set<TeamDto> teams;
    private Set<UserDto> members;
    private UserDto manager;
}
