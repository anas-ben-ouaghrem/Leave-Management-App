package com.telnet.leaveapp.telnetleavemanager.user;

import com.telnet.leaveapp.telnetleavemanager.auth.AuthenticationService;
import com.telnet.leaveapp.telnetleavemanager.auth.RegisterRequest;
import com.telnet.leaveapp.telnetleavemanager.auth.TwoFactorAuthenticationService;
import com.telnet.leaveapp.telnetleavemanager.config.JwtService;
import com.telnet.leaveapp.telnetleavemanager.entities.OrganizationalUnit;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.OrganizationalUnitRepository;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.services.MailingService;
import io.jsonwebtoken.Claims;
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
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final JwtService jwtService;
    private final TwoFactorAuthenticationService tfaService;
    private final AuthenticationService authenticationService;
    private final MailingService mailingService;

    public void addUser(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
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
        this.mailingService.sendMail(savedUser.getEmail(), "Account created", "Your account has been created\n You may now login to the application with the following credentials: \n Email: " + savedUser.getEmail() + "\n Password: " + request.getPassword());
    }

    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with given email not found"));
    }

    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id:" + id + " not found"));
    }

    public User updateUser(String currentUserEmail, String targetUserEmail, RegisterRequest request) {
        User currentUser = repository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User targetUser = repository.findByEmail(targetUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser.getRole() != Role.ADMIN ) {
            if (targetUser.getTeam() != null) {
                if (currentUser != targetUser.getTeam().getManager() || currentUser != targetUser.getOrganizationalUnit().getManager() || currentUser != targetUser) {
                    throw new UnauthorizedActionException("You are not authorized to update this user");
                }

            }
        }

        targetUser.setPhone(request.getPhone() != null ? request.getPhone() : targetUser.getPhone());
        targetUser.setFirstName(request.getFirstName() != null ? request.getFirstName() : targetUser.getFirstName());
        targetUser.setLastName(request.getLastName() != null ? request.getLastName() : targetUser.getLastName());
        targetUser.setEmail(request.getEmail() != null ? request.getEmail() : targetUser.getEmail());
        targetUser.setRole(request.getRole() != null ? request.getRole() : targetUser.getRole());
        targetUser.setMfaEnabled(request.isMfaEnabled());

        this.mailingService.sendMail(targetUser.getEmail(), "Account updated", "Your account has been updated\n Please contact your administrator if you did not perform this action");

        return repository.save(targetUser);
    }

    public void deleteUser(String email) {
        User userToBeDeleted = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        if (userToBeDeleted.getOrganizationalUnit() != null) {
            OrganizationalUnit orgUnit = this.organizationalUnitRepository.findByManager(userToBeDeleted)
                    .orElseThrow(() -> new RuntimeException("Organizational unit with manager " + userToBeDeleted.getFirstName() + " " + userToBeDeleted.getLastName() + " not found"));
            orgUnit.setManager(null);
            this.organizationalUnitRepository.saveAndFlush(orgUnit);
        }
       
        repository.delete(userToBeDeleted);
        this.mailingService.sendMail(email, "Account deleted", "Your account has been deleted\n Please contact your administrator if you did not perform this action");
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public void resetPassword(String email,String oldPassword, String newPassword) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

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

    @Scheduled(cron = "0 0 1 * * ?")
    public void resetExternalActivities() {
        List<User> users = repository.findAll();
        for (User user : users) {
            user.setExternalActivitiesLimit(2);
            repository.save(user);
            this.mailingService.sendMail(user.getEmail(), "External activities reset", "Your external activities have been reset");
        }
    }


    public void deleteUserById(Long id) {
        User userToBeDeleted = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        repository.delete(userToBeDeleted);
        this.mailingService.sendMail(userToBeDeleted.getEmail(), "Account deleted", "Your account has been deleted\n Please contact your administrator if you did not perform this action");
    }

    // FORGOT PASSWORD

    public void sendPasswordResetEmail(String userEmail) {
        User user = repository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a password reset token and include it in the email
        String resetToken = jwtService.generatePasswordResetToken(user);

        // Construct the password reset URL with the JWT token
        String resetUrl = "http://localhost:4200/pages/authentication/reset-password-v2?token=" + resetToken;

        // Send the email with the password reset link
        String subject = "Password Reset Request";
        String body = "To reset your password, click the link below:\n\n" + resetUrl;
        mailingService.sendMail(userEmail, subject, body);
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        Claims claims = jwtService.extractAllClaims(token);

        // Extract user information from the token
        String email = claims.getSubject();
        Long userId = claims.get("id", Long.class);

        // Retrieve user from the database
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate the token and email
        if (!jwtService.isPasswordResetTokenValid(token, user) || !email.equals(user.getEmail())) {
            throw new IllegalStateException("Invalid password reset token");
        }

        // Perform password reset logic
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // Generate a new JWT token after password reset
        String newJwtToken = jwtService.generateToken(user);
        authenticationService.revokeAllUserTokens(user);
        authenticationService.saveUserToken(newJwtToken, user);

        // Save the updated user entity
        repository.save(user);

        // Send email notification about password reset
        this.mailingService.sendMail(email, "Password Reset", "Your password has been reset successfully");
    }

    public List<User> getUsersByManager(String managerEmail) {
        try {
            User manager = repository.findByEmail(managerEmail)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            Team team = manager.getTeam();
            return team.getMembers();
        } catch (Exception e) {
            throw new IllegalStateException("User not found");
        }
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void resetOnLeave() {
        List<User> users = repository.findAll();
        for (User user : users) {
            if (user.getReturnDate() != null && user.getReturnDate().isBefore(java.time.LocalDateTime.now())) {
                user.setOnLeave(false);
                user.setReturnDate(null);
                this.mailingService.sendMail(user.getEmail(), "Welcome Back", "Hello " + user.getFirstName() + " " + user.getLastName() + "\n Welcome back to work");
                repository.saveAndFlush(user);
            }
        }
    }
}
