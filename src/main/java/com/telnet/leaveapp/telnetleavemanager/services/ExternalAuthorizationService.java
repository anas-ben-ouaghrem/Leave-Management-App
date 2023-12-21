package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.ExternalAuthorizationRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.ExternalAuthorization;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.ExternalAuthorizationRepository;
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

    public ExternalAuthorization createExternalAuthorization(String currentUserEmail, ExternalAuthorizationRequest request) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser != user && currentUser.getRole() == Role.USER) {
            throw new UnauthorizedActionException("You are not authorized to perform this action");
        }

        ExternalAuthorization externalAuthorization = ExternalAuthorization.builder()
                .leaveDuration(request.getLeaveDuration())
                .startDate(request.getDate())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        return externalAuthorizationRepository.save(externalAuthorization);
    }

    public void treatExternalAuthorization(Long id, Status status, String currentUserEmail) {
        ExternalAuthorization externalAuthorization = externalAuthorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("External authorization not found"));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser != externalAuthorization.getUser().getOrganizationalUnit().getManager() || currentUser.getRole().equals(Role.ADMIN) || currentUser != externalAuthorization.getUser().getTeam().getManager()) {
            throw new UnauthorizedActionException("You are not authorized to treat this external authorization");
        }
        if (status == Status.ACCEPTED) {
            currentUser.setExternalActivitiesLimit(currentUser.getExternalActivitiesLimit() - 1);
            userRepository.save(currentUser);
            log.info("External authorization accepted");
        } else if (status == Status.REJECTED) {
            log.info("External authorization rejected");
        }
        externalAuthorization.setStatus(status);
        externalAuthorizationRepository.save(externalAuthorization);
    }

    public List<ExternalAuthorization> getAllExternalAuthorizations() {
        return externalAuthorizationRepository.findAll();
    }

    public ExternalAuthorization getExternalAuthorizationById(Long id) {
        return externalAuthorizationRepository.findById(id).orElseThrow(() -> new RuntimeException("External authorization not found"));
    }

    public void deleteExternalAuthorization(Long id) {
        externalAuthorizationRepository.deleteById(id);
        log.info("External authorization deleted");
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
        }
    }
}
