package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamLeaveRepository extends JpaRepository<TeamLeave,Long> {
    List<TeamLeave> findByTeamId(Long teamId);

    List<TeamLeave> findByTeamAndStatus(Team team, Status status);
}
