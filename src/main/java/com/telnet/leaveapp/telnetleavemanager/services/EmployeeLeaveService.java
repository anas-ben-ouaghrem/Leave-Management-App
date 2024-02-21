package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.LeaveRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.*;
import com.telnet.leaveapp.telnetleavemanager.exceptions.InsufficientLeaveBalanceException;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.EmployeeLeaveRepository;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeLeaveService {

    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final UserRepository userRepository;
    private final MailingService mailingService;

    public void createLeaveRequest(String currentUserEmail,LeaveRequest leaveRequest) {
        validateLeaveRequest(leaveRequest);
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(leaveRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmployeeLeave leave = EmployeeLeave.builder()
                .user(user)
                .leaveType(leaveRequest.getLeaveType())
                .exceptionalLeaveType(leaveRequest.getExceptionalLeaveType())
                .startDate(leaveRequest.getStartDate())
                .status(Status.PENDING)
                .reason(leaveRequest.getReason())
                .createdAt(LocalDateTime.now())
                .timeOfDay(leaveRequest.getTimeOfDay())
                .build();

        if (leaveRequest.getLeaveType() == LeaveType.PERSONAL_LEAVE || leaveRequest.getLeaveType() == LeaveType.SICK_LEAVE) {
            leave.setExceptionalLeaveType(ExceptionalLeaveType.NONE);
            if (leaveRequest.getLeaveType() == LeaveType.PERSONAL_LEAVE && user.getLeaveDays() <= 0 || leaveRequest.getLeaveType() == LeaveType.HALF_DAY && user.getLeaveDays() <= 0) {
                throw new InsufficientLeaveBalanceException("You don't have enough leave days");
            }
            if (leaveRequest.getLeaveType() == LeaveType.SICK_LEAVE && currentUser.getRole()!= Role.ADMIN) {
                throw new UnauthorizedActionException("You are not authorized to create sick leave requests");
            }
            // For personal leave, set the end date directly from the DTO
            leave.setEndDate(leaveRequest.getEndDate());
        } else {
            // Calculate duration based on the leave type and set the end date
            calculateDurationAndSetEndDate(leave);
        }
        if (currentUser.getRole() == Role.ADMIN ) {
            leave.setStatus(Status.ACCEPTED);
        }
        employeeLeaveRepository.save(leave);
        this.mailingService.sendMail(user.getEmail(),"Leave request created", "Your leave request has been created");
        if(Objects.equals(user.getEmail(), currentUser.getEmail()) && currentUser.getRole() == Role.MANAGER) {
            this.mailingService.sendMail(currentUser.getTeam().getOrganizationalUnit().getManager().getEmail(),"Leave request created", "Leave request for " + user.getEmail() + " has been created");
        } else this.mailingService.sendMail(currentUser.getEmail(),"Leave request created", "Leave request for " + user.getEmail() + " has been created");
    }

    private void calculateDurationAndSetEndDate(EmployeeLeave leave) {
        LeaveType leaveType = leave.getLeaveType();

        if (leaveType == LeaveType.EXCEPTIONAL_LEAVE) {
            ExceptionalLeaveType exceptionalLeaveType = leave.getExceptionalLeaveType();
            int duration = exceptionalLeaveType.getDuration();
            leave.setEndDate(leave.getStartDate().plusDays(duration));
        } else if (leaveType == LeaveType.HALF_DAY) {
            leave.setEndDate(leave.getStartDate().plusDays(1));
        }
    }

    public EmployeeLeave treatLeaveRequest(String currentUserEmail, Long leaveRequestId, String status) {

        EmployeeLeave leaveRequest = employeeLeaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave Request not Found"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User userRequestingLeave = leaveRequest.getUser();

        if ( currentUser.getRole() == Role.USER ) {
            throw new IllegalStateException("You are not authorized to treat this leave request.");
        }

        if (leaveRequest.getLeaveType() == LeaveType.PERSONAL_LEAVE && userRequestingLeave.getLeaveDays() < Duration.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()).toDays()) {
            leaveRequest.setStatus(Status.REJECTED);
            throw new IllegalStateException("User does not have enough leave balance.");
        }

        // Consider additional conditions or business rules for setting the status
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            List<EmployeeLeave> pendingLeaveRequests = employeeLeaveRepository
                    .findByUserAndStatus(userRequestingLeave, Status.PENDING);

            for (EmployeeLeave pendingLeave : pendingLeaveRequests) {
                if (!pendingLeave.getId().equals(leaveRequestId)) {
                    // Erase or update status for other pending leave requests
                    // Here, assuming you want to erase them, you can use your own logic
                    employeeLeaveRepository.delete(pendingLeave);
                }
            }
            leaveRequest.setStatus(Status.ACCEPTED);
            if (leaveRequest.getStartDate().isBefore(LocalDateTime.now()) && leaveRequest.getEndDate().isAfter(LocalDateTime.now())) {
                userRequestingLeave.setOnLeave(true);
            }
            userRequestingLeave.setReturnDate(leaveRequest.getEndDate());
            userRepository.save(userRequestingLeave);
            updateLeaveBalance(leaveRequest, userRequestingLeave);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            leaveRequest.setStatus(Status.REJECTED);
            // You may want to handle additional actions for a rejected leave request
        } else {
            throw new IllegalArgumentException("Invalid status provided.");
        }
        this.mailingService.sendMail(userRequestingLeave.getEmail(),"Leave request treated", "Your leave request with id: " + leaveRequestId + " has been " + status);
        return employeeLeaveRepository.save(leaveRequest);
    }

    private void updateLeaveBalance(EmployeeLeave leaveRequest, User userRequestingLeave) {

        double currentBalance = userRequestingLeave.getLeaveDays();
        double requestedDays = Duration.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()).toDays();
        if(leaveRequest.getLeaveType() == LeaveType.HALF_DAY) requestedDays = 0.5f;
        userRequestingLeave.setLeaveDays(currentBalance - requestedDays);
        userRepository.saveAndFlush(userRequestingLeave);
    }

    public void deleteLeaveRequestEmployee(Long id) {
        EmployeeLeave currentLeaveRequest = employeeLeaveRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Leave request not found!"));
        if (currentLeaveRequest.getStatus() != Status.PENDING) {
            throw new UnauthorizedActionException("This Leave Request has already been processed!");
        }
        employeeLeaveRepository.delete(currentLeaveRequest);
    }

    public EmployeeLeave updateLeaveRequestEmployee(String currentUserEmail, Long leaveRequestId, LeaveRequest leaveRequest) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        EmployeeLeave existingLeaveRequest = employeeLeaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (existingLeaveRequest.getStatus() != Status.PENDING || currentUser!=existingLeaveRequest.getUser()) {
            throw new UnauthorizedActionException("Unauthorized action");
        }
        return updateFields(leaveRequest, existingLeaveRequest);
    }

    private EmployeeLeave updateFields(LeaveRequest leaveRequest, EmployeeLeave existingLeaveRequest) {
        existingLeaveRequest.setReason(leaveRequest.getReason() == null ? existingLeaveRequest.getReason() : leaveRequest.getReason());
        existingLeaveRequest.setLeaveType(leaveRequest.getLeaveType() == null ? existingLeaveRequest.getLeaveType() : leaveRequest.getLeaveType());
        existingLeaveRequest.setExceptionalLeaveType(leaveRequest.getExceptionalLeaveType() == null ? existingLeaveRequest.getExceptionalLeaveType() : leaveRequest.getExceptionalLeaveType());
        existingLeaveRequest.setLeaveType(leaveRequest.getLeaveType() == null ? existingLeaveRequest.getLeaveType() : leaveRequest.getLeaveType());
        existingLeaveRequest.setStartDate(leaveRequest.getStartDate() == null ? existingLeaveRequest.getStartDate() : leaveRequest.getStartDate());
        existingLeaveRequest.setEndDate(leaveRequest.getEndDate() == null ? existingLeaveRequest.getEndDate() : leaveRequest.getEndDate());
        this.mailingService.sendMail(existingLeaveRequest.getUser().getEmail(),"Leave request updated", "Your leave request with id: " + existingLeaveRequest.getId() + " has been updated");
        return employeeLeaveRepository.save(existingLeaveRequest);
    }

    public EmployeeLeave updateLeaveRequest(Long leaveRequestId, LeaveRequest leaveRequest) {
        EmployeeLeave existingLeaveRequest = employeeLeaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (existingLeaveRequest.getStatus() != Status.PENDING) {
            throw new UnauthorizedActionException("The Leave Request has already been processed");
        }
        return updateFields(leaveRequest, existingLeaveRequest);
    }

    public void deleteLeaveRequest(Integer leaveRequestId) {
        employeeLeaveRepository.deleteById(leaveRequestId.longValue());
    }

    public EmployeeLeave getLeaveRequestById(Long leaveRequestId) {
        return employeeLeaveRepository.findById(leaveRequestId).orElse(null);
    }

    public List<EmployeeLeave> getAllLeaveRequests() {
        return employeeLeaveRepository.findAll();
    }

    public List<EmployeeLeave> getLeaveRequestsByUserId(Integer userId) {
        return employeeLeaveRepository.findAllByUser_Id(userId);
    }


    private void validateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getUserEmail() == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (leaveRequest.getLeaveType() == null) {
            throw new IllegalArgumentException("Leave type is required");
        }
        if (leaveRequest.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (leaveRequest.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
    }

    public List<EmployeeLeave> getLeaveRequestsByManager(String managerEmail) {
        List<EmployeeLeave> leaveRequests = new ArrayList<>();
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Team team = manager.getTeam();
        for (User user : team.getMembers()) {
            leaveRequests.addAll(employeeLeaveRepository.findAllByUser_Id(user.getId()));
        }
        return leaveRequests;
    }

    @Scheduled(fixedDelay = 60*60*1000)
    @Transactional
    public void checkOnLeaveUsers() {
        log.info("Scheduled task started.");
        List<EmployeeLeave> leaveRequests = employeeLeaveRepository.findAll().stream()
                .filter(leaveRequest -> leaveRequest.getStatus() == Status.ACCEPTED)
                .toList();
        log.info("Leave requests: " + leaveRequests);
        for (EmployeeLeave leaveRequest : leaveRequests) {
                if (leaveRequest.getEndDate().isBefore(LocalDateTime.now())) {
                    log.info("User " + leaveRequest.getUser().getEmail() + " has returned from leave");
                    leaveRequest.getUser().setOnLeave(false);
                    leaveRequest.getUser().setReturnDate(null);
                    userRepository.saveAndFlush(leaveRequest.getUser());
                } else if (leaveRequest.getStartDate().isBefore(LocalDateTime.now())) {
                    log.info("User " + leaveRequest.getUser().getEmail() + " is on leave");
                    leaveRequest.getUser().setOnLeave(true);
                    leaveRequest.getUser().setReturnDate(leaveRequest.getEndDate());
                    userRepository.saveAndFlush(leaveRequest.getUser());
                }

        }
        log.info("Scheduled task finished.");
    }
}
