package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.ExternalAuthorization;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamExitPermission;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamExitPermissionRepository extends JpaRepository<TeamExitPermission, Long> {

    List<TeamExitPermission> findByTeamId(Long team_id);

    List<TeamExitPermission> findByTeam_Manager(User manager);

    List<TeamExitPermission> findByTeam(Team team);

    List<TeamExitPermission> findByTeamAndStatus(Team team, Status status);
}
