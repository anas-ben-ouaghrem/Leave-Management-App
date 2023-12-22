package com.telnet.leaveapp.telnetleavemanager.dto;

import com.telnet.leaveapp.telnetleavemanager.entities.ExceptionalLeaveType;
import com.telnet.leaveapp.telnetleavemanager.entities.LeaveType;
import com.telnet.leaveapp.telnetleavemanager.entities.TimeOfDay;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class LeaveRequest {

    private String userEmail;
    private LeaveType leaveType;
    private ExceptionalLeaveType exceptionalLeaveType = ExceptionalLeaveType.NONE;
    @Builder.Default
    private TimeOfDay timeOfDay = TimeOfDay.INAPPLICABLE;
    @Builder.Default
    private LocalDateTime startDate = LocalDateTime.now();
    @Nullable
    @Builder.Default
    private LocalDateTime endDate = LocalDateTime.now();
}
