package com.telnet.leaveapp.telnetleavemanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private Status status;
    private LocalDateTime createdAt;
    @ManyToOne
    private Team team;
}
