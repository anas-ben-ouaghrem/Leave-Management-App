package com.telnet.leaveapp.telnetleavemanager.entities;

import com.telnet.leaveapp.telnetleavemanager.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeave {

    @Id
    @SequenceGenerator(
            name = "leave_id_seq",
            sequenceName = "leave_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "leave_id_seq"
    )
    private Long id;
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ExceptionalLeaveType exceptionalLeaveType = ExceptionalLeaveType.NONE;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TimeOfDay timeOfDay = TimeOfDay.INAPPLICABLE;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
