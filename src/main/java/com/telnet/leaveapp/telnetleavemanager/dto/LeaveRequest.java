package com.telnet.leaveapp.telnetleavemanager.dto;

import com.telnet.leaveapp.telnetleavemanager.entities.ExceptionalLeaveType;
import com.telnet.leaveapp.telnetleavemanager.entities.LeaveType;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class LeaveRequest {

    private Long userId;
    private LeaveType leaveType;
    private ExceptionalLeaveType exceptionalLeaveType = ExceptionalLeaveType.NONE;
    private LocalDateTime startDate;
    @Nullable
    private LocalDateTime endDate;
}
