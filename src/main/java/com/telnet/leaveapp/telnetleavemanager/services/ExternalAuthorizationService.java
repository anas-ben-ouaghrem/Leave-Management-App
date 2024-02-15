package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.ExternalAuthorizationRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.ExternalAuthorization;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.exceptions.InsufficientAuthorizationBalanceException;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.ExternalAuthorizationRepository;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalAuthorizationService {

    private final ExternalAuthorizationRepository externalAuthorizationRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MailingService mailingService;

    public ExternalAuthorization createExternalAuthorization(String currentUserEmail, ExternalAuthorizationRequest request) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser != user && currentUser.getRole() == Role.USER) {
            throw new UnauthorizedActionException("You are not authorized to perform this action");
        }

        if (user.getExternalActivitiesLimit() <= 0) {
            throw new InsufficientAuthorizationBalanceException("You have reached the limit of Exit Permissions");
        }

        ExternalAuthorization externalAuthorization = ExternalAuthorization.builder()
                .leaveDuration(request.getLeaveDuration())
                .reason(request.getReason())
                .startDate(request.getDate())
                .endDate(request.getDate().plusMinutes(request.getLeaveDuration().getDuration()))
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        this.mailingService.sendMail(user.getEmail(),"Exit Permissions request created", "Your Exit Permissions request has been created");
        return externalAuthorizationRepository.save(externalAuthorization);
    }

    public void treatExternalAuthorization(Long id, Status status, String currentUserEmail) {
        ExternalAuthorization externalAuthorization = externalAuthorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exit Permission not found"));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser.getRole() != Role.ADMIN ) {
            if (currentUser != externalAuthorization.getUser().getOrganizationalUnit().getManager() || currentUser != externalAuthorization.getUser().getTeam().getManager()) {
                throw new UnauthorizedActionException("You are not authorized to treat this Exit Permissions");
            }
        }

        User user = externalAuthorization.getUser();
        if (status == Status.ACCEPTED) {
            List<ExternalAuthorization> pendingExitPermissions = externalAuthorizationRepository
                    .findByUserAndStatus(user, Status.PENDING);

            for (ExternalAuthorization pendingExitPermission : pendingExitPermissions) {
                if (!pendingExitPermission.getId().equals(id)) {
                    // Erase or update status for other pending exit permissions
                    // Here, assuming you want to erase them, you can use your own logic
                    externalAuthorizationRepository.delete(pendingExitPermission);
                }
            }
            user.setExternalActivitiesLimit(user.getExternalActivitiesLimit() - 1);
            userRepository.saveAndFlush(user);
            log.info("Exit Permissions accepted");
        } else if (status == Status.REJECTED) {
            log.info("Exit Permissions rejected");
        }
        externalAuthorization.setStatus(status);
        externalAuthorizationRepository.saveAndFlush(externalAuthorization);
        this.mailingService.sendMail(user.getEmail(),"Exit Permissions treated", "Your Exit Permissions with id " + id + " has been " + status);
    }

    public List<ExternalAuthorization> getAllExternalAuthorizations() {
        return externalAuthorizationRepository.findAll();
    }

    public ExternalAuthorization getExternalAuthorizationById(Long id) {
        return externalAuthorizationRepository.findById(id).orElseThrow(() -> new RuntimeException("Exit Permissions not found"));
    }

    public void deleteExternalAuthorization(Long id) {
        externalAuthorizationRepository.deleteById(id);
        log.info("Exit Permissions deleted");
    }

    public List<ExternalAuthorization> getExternalAuthorizationsForUser(Integer userId) {
        return externalAuthorizationRepository.findByUserId(userId);
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void resetExternalAuthorizationLimit() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setExternalActivitiesLimit(2);
            userRepository.save(user);
            this.mailingService.sendMail(user.getEmail(), "Exit Permissions limit reset", "Your Exit Permissions limit has been reset");
        }
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

    public List<ExternalAuthorization> getExternalAuthorizationsByTeamManager(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return externalAuthorizationRepository.findByUser_Team_Manager(manager);
    }

    public List<ExternalAuthorization> getExternalAuthorizationsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return externalAuthorizationRepository.findByUser(user);
    }

    public ExternalAuthorization updateExternalAuthorization(Long id, ExternalAuthorizationRequest request) {
        ExternalAuthorization externalAuthorization = externalAuthorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exit Permissions not found"));
        externalAuthorization.setLeaveDuration(request.getLeaveDuration());
        externalAuthorization.setStartDate(request.getDate());
        externalAuthorization.setReason(request.getReason());
        externalAuthorization.setEndDate(request.getDate().plusMinutes(request.getLeaveDuration().getDuration()));
        externalAuthorizationRepository.saveAndFlush(externalAuthorization);
        return externalAuthorization;
    }
}
