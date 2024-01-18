package com.telnet.leaveapp.telnetleavemanager.user;

import com.telnet.leaveapp.telnetleavemanager.auth.AuthenticationService;
import com.telnet.leaveapp.telnetleavemanager.auth.RegisterRequest;
import com.telnet.leaveapp.telnetleavemanager.auth.TwoFactorAuthenticationService;
import com.telnet.leaveapp.telnetleavemanager.config.JwtService;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.services.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorAuthenticationService tfaService;
    private final AuthenticationService authenticationService;
    private final MailingService mailingService;

    public void addUser(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gender(request.getGender())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .mfaEnabled(false)
                .secret(null)
                .build();

        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        jwtService.generateRefreshToken(user);
        authenticationService.saveUserToken(jwtToken, savedUser);
        this.mailingService.sendMail(savedUser.getEmail(), "Account created", "Your account has been created\n You may now login to the application");
    }

    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(String currentUserEmail, String targetUserEmail, RegisterRequest request) {
        User currentUser = repository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User targetUser = repository.findByEmail(targetUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser.getRole() != Role.ADMIN || currentUser != targetUser.getTeam().getManager() || currentUser != targetUser.getOrganizationalUnit().getManager() || currentUser != targetUser) {
            throw new RuntimeException("You are not authorized to update this user");
        }
        currentUser.setPhone(request.getPhone() != null ? request.getPhone() : currentUser.getPhone());
        currentUser.setFirstName(request.getFirstname() != null ? request.getFirstname() : currentUser.getFirstName());
        currentUser.setLastName(request.getLastname() != null ? request.getLastname() : currentUser.getLastName());
        currentUser.setEmail(request.getEmail() != null ? request.getEmail() : currentUser.getEmail());
        currentUser.setRole(request.getRole() != null ? request.getRole() : currentUser.getRole());
        currentUser.setMfaEnabled(request.isMfaEnabled());

        this.mailingService.sendMail(currentUser.getEmail(), "Account updated", "Your account has been updated\n Please contact your administrator if you did not perform this action");

        return repository.save(targetUser);
    }

    public void deleteUser(String email) {
        User userToBeDeleted = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        repository.delete(userToBeDeleted);
        this.mailingService.sendMail(email, "Account deleted", "Your account has been deleted\n Please contact your administrator if you did not perform this action");
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public void resetPassword(String email, String newPassword) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        var jwtToken = jwtService.generateToken(user);
        authenticationService.revokeAllUserTokens(user);
        authenticationService.saveUserToken(jwtToken, user);
        // Save the updated user entity
        repository.save(user);
        this.mailingService.sendMail(email, "Password reset", "Your password has been reset\n Please contact your administrator if you did not perform this action");
    }

    public void affectTeamToUser(String userEmail, String teamName) {
        User user = getUserByEmail(userEmail);
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(() -> new RuntimeException("Team not found!"));
        user.setTeam(team);
        repository.save(user);
        this.mailingService.sendMail(userEmail, "Team affected", "You have been affected to the team " + teamName + "\n Your manager is " + team.getManager().getFirstName() + " " + team.getManager().getLastName());
        log.info(user.getId() + " added to team" + team.getName());
    }

    public void removeUserFromTeam(String userEmail, String teamName) {
        User user = getUserByEmail(userEmail);
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(() -> new RuntimeException("Team not found!"));
        team.getMembers().remove(user);
        user.setTeam(null);
        repository.save(user);
        teamRepository.save(team);
        this.mailingService.sendMail(userEmail, "Team removed", "You have been removed from the team " + teamName);
    }

    @Scheduled(cron = "0 0 0 1 1 ?")
    public void resetAnnualLeaves() {
        List<User> users = repository.findAll();
        for (User user : users) {
            user.setLeaveDays(26);
            repository.save(user);
            this.mailingService.sendMail(user.getEmail(), "Annual leaves reset", "Your annual leaves have been reset");
        }
    }
}
