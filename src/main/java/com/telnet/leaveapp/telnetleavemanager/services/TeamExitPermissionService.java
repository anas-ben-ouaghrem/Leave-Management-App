package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamExitPermissionRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamExitPermission;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamExitPermissionRepository;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamExitPermissionService {

    private final TeamExitPermissionRepository teamExitPermissionRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MailingService mailingService;

    public TeamExitPermission createTeamExitPermission(String currentUserEmail, TeamExitPermissionRequest request) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Team team = teamRepository.findByName(request.getTeamName())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (currentUser != team.getManager() && currentUser.getRole() == Role.USER) {
            throw new UnauthorizedActionException("You are not authorized to perform this action");
        }

        TeamExitPermission teamExitPermission = TeamExitPermission.builder()
                .leaveDuration(request.getLeaveDuration())
                .reason(request.getReason())
                .startDate(request.getDate())
                .endDate(request.getDate().plusMinutes(request.getLeaveDuration().getDuration()))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .team(team)
                .build();
        this.mailingService.sendMail(team.getManager().getEmail(),"Exit Permissions request created", "Your Exit Permissions request has been created");
        return teamExitPermissionRepository.save(teamExitPermission);
    }

    public void treatTeamExitPermission(Long id, Status status, String currentUserEmail) {
        TeamExitPermission teamExitPermission = teamExitPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exit Permission not found"));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser.getRole() != Role.ADMIN ) {
            if (currentUser != teamExitPermission.getTeam().getManager() ) {
                throw new UnauthorizedActionException("You are not authorized to treat this Exit Permissions");
            }
        }

        Team team = teamExitPermission.getTeam();

        teamExitPermission.setStatus(status);
        teamExitPermissionRepository.saveAndFlush(teamExitPermission);
        this.mailingService.sendMail(team.getManager().getEmail(),"Team Exit Permission treated", "Your Exit Permissions with id " + id + " has been " + status);
    }

    public List<TeamExitPermission> getAllExternalAuthorizations() {
        return teamExitPermissionRepository.findAll();
    }

    public TeamExitPermission getTeamExitPermissionById(Long id) {
        return teamExitPermissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Exit Permissions not found"));
    }

    public void deleteExitPermission(Long id) {
        teamExitPermissionRepository.deleteById(id);
        log.info("Exit Permissions deleted");
    }

    public List<TeamExitPermission> getTeamExitPermissionByTeamId(Long userId) {
        return teamExitPermissionRepository.findByTeamId(userId);
    }

//    public List<ExternalAuthorization> getExternalAuthorizationsByManager(String managerEmail) {
//        List<ExternalAuthorization> externalAuthorizations = new ArrayList<>();
//        User manager = userRepository.findByEmail(managerEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Team team = manager.getTeam();
//        for (User user : team.getMembers()) {
//            externalAuthorizations.addAll(user.getExternalAuthorizations());
//        }
//        return externalAuthorizations;
//    }
//
//    public List<ExternalAuthorization> getExternalAuthorizationsByTeam(String teamName) {
//        List<ExternalAuthorization> externalAuthorizations = new ArrayList<>();
//        Team team = teamRepository.findByName(teamName)
//                .orElseThrow(() -> new RuntimeException("Team not found"));
//        for (User user : team.getMembers()) {
//            externalAuthorizations.addAll(user.getExternalAuthorizations());
//        }
//        return externalAuthorizations;
//    }

    public List<TeamExitPermission> getExternalAuthorizationsByTeamManager(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return teamExitPermissionRepository.findByTeam_Manager(manager);
    }

    public List<TeamExitPermission> getTeamExitPermissionByTeam(String teamName) {
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return teamExitPermissionRepository.findByTeam(team);
    }

    public TeamExitPermission updateTeamExitPermission(Long id, TeamExitPermissionRequest request) {
        TeamExitPermission teamExitPermission = teamExitPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exit Permissions not found"));
        teamExitPermission.setLeaveDuration(request.getLeaveDuration());
        teamExitPermission.setStartDate(request.getDate());
        teamExitPermission.setReason(request.getReason());
        teamExitPermission.setEndDate(request.getDate().plusMinutes(request.getLeaveDuration().getDuration()));
        teamExitPermissionRepository.saveAndFlush(teamExitPermission);
        return teamExitPermission;
    }
}

