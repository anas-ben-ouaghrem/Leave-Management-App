package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamLeaveRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamLeave;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamLeaveRepository;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamLeaveService {

    private final TeamLeaveRepository teamLeaveRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MailingService mailingService;

    public TeamLeave createTeamLeave(String currentUserEmail, TeamLeaveRequest request) {

        Team team = teamRepository.findByName(request.getName())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not Found"));

        if (currentUser.getRole() == Role.USER ) {
            throw new UnauthorizedActionException("You are unauthorized to create team leave requests for this team");
        }

        if (Objects.isNull(request.getStartDate()) || Objects.isNull(request.getEndDate())) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        TeamLeave teamLeave = TeamLeave.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .team(team)
                .status(Status.PENDING)
                .build();
        mailingService.sendMail(team.getOrganizationalUnit().getManager().getEmail(), "Leave Request", "The Team manager " + team.getManager().getFirstName() + " " + team.getManager().getLastName() + " has requested a leave for his team " + team.getName() + " for the period " + teamLeave.getStartDate() + " - " + teamLeave.getEndDate() + ". Please check the leave request and take the appropriate action.");
        mailingService.sendMail(team.getManager().getEmail(), "Leave Request", "You have requested a leave for your team " + team.getName() + " for the period " + teamLeave.getStartDate() + " - " + teamLeave.getEndDate() + ".");
        return teamLeaveRepository.save(teamLeave);
    }

    public TeamLeave treatTeamLeaveRequest(String currentUserEmail, Long leaveRequestId, String status) {

        TeamLeave leaveRequest = teamLeaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if ( currentUser.getRole() == Role.USER) {
            throw new IllegalStateException("You are not authorized to treat this leave request.");
        }
        // Consider additional conditions or business rules for setting the status
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            // Erase other pending team leave requests from the same team
            List<TeamLeave> pendingTeamLeaveRequests = teamLeaveRepository
                    .findByTeamAndStatus(leaveRequest.getTeam(), Status.PENDING);

            for (TeamLeave pendingLeave : pendingTeamLeaveRequests) {
                if (!pendingLeave.getId().equals(leaveRequestId)) {
                    // Erase or update status for other pending leave requests
                    // Here, assuming you want to erase them, you can use your own logic
                    teamLeaveRepository.delete(pendingLeave);
                }
            }

            leaveRequest.setStatus(Status.ACCEPTED);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            leaveRequest.setStatus(Status.REJECTED);
            // You may want to handle additional actions for a rejected leave request
        } else {
            throw new IllegalArgumentException("Invalid status provided.");
        }

        this.mailingService.sendMail(leaveRequest.getTeam().getOrganizationalUnit().getManager().getEmail(), "Team Leave Request Update", "The team manager of team  " + leaveRequest.getTeam().getName() + " " + leaveRequest.getTeam().getManager().getFirstName() + " " + leaveRequest.getTeam().getManager().getLastName() + " has updated his team leave request for the period " + leaveRequest.getStartDate() + " - " + leaveRequest.getEndDate() + ". Please check the leave request and take the appropriate action.");
        this.mailingService.sendMail(leaveRequest.getTeam().getManager().getEmail(), "Team Leave Request Update", "You have updated your team leave request for the period " + leaveRequest.getStartDate() + " - " + leaveRequest.getEndDate() + ".");
        return teamLeaveRepository.save(leaveRequest);
    }

    public void deleteLeaveRequestTeamLead(Long id) {
        TeamLeave currentLeaveRequest = teamLeaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found!"));
        if (currentLeaveRequest.getStatus() != Status.PENDING) {
            throw new UnauthorizedActionException("This Leave Request has already been processed!");
        }
        teamLeaveRepository.delete(currentLeaveRequest);
    }

    public TeamLeave updateLeaveRequestEmployee(Long leaveRequestId) {

        TeamLeave leaveRequest = teamLeaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        TeamLeave existingLeaveRequest = teamLeaveRepository.findById(leaveRequest.getId())
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (existingLeaveRequest.getStatus() != Status.PENDING) {
            throw new UnauthorizedActionException("The Leave Request has already been processed");
        }
        existingLeaveRequest.setStartDate(leaveRequest.getStartDate());
        existingLeaveRequest.setEndDate(leaveRequest.getEndDate());

        this.mailingService.sendMail(leaveRequest.getTeam().getManager().getEmail(), "Leave Request Update", "The employee " + leaveRequest.getTeam().getManager().getFirstName() + " " + leaveRequest.getTeam().getManager().getLastName() + " has updated his leave request for the period " + leaveRequest.getStartDate() + " - " + leaveRequest.getEndDate() + ". Please check the leave request and take the appropriate action.");
        return teamLeaveRepository.save(existingLeaveRequest);
    }

    public List<TeamLeave> getAllTeamLeaves() {
        return teamLeaveRepository.findAll();
    }

    public TeamLeave getTeamLeaveById(Long id) {
        return teamLeaveRepository.findById(id).orElse(null);
    }

    public void deleteTeamLeave(Long id) {
        teamLeaveRepository.deleteById(id);
    }

    public List<TeamLeave> getTeamLeavesForTeam(Long teamId) {
        return teamLeaveRepository.findByTeamId(teamId);
    }

    public TeamLeave updateTeamLeave(Long id, TeamLeaveRequest request) {
        TeamLeave teamLeave = teamLeaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        teamLeave.setStartDate(request.getStartDate());
        teamLeave.setEndDate(request.getEndDate());
        teamLeave.setReason(request.getReason());
        teamLeaveRepository.save(teamLeave);
        mailingService.sendMail(teamLeave.getTeam().getManager().getEmail(), "Leave Request Update", "The employee " + teamLeave.getTeam().getManager().getFirstName() + " " + teamLeave.getTeam().getManager().getLastName() + " has updated his leave request for the period " + teamLeave.getStartDate() + " - " + teamLeave.getEndDate() + ". Please check the leave request and take the appropriate action.");
        return teamLeave;
    }
}


