package com.telnet.leaveapp.telnetleavemanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TeamLeaveRequest {

    private String teamName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
}
