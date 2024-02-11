package com.telnet.leaveapp.telnetleavemanager.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private UserDto manager;
    private List<UserDto> members;
    private Long organizationalUnitId;
}
