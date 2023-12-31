package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.LeaveRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.*;
import com.telnet.leaveapp.telnetleavemanager.exceptions.InsufficientLeaveBalanceException;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.EmployeeLeaveRepository;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeLeaveService {

    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final UserRepository userRepository;

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
                .createdAt(LocalDateTime.now())
                .timeOfDay(leaveRequest.getTimeOfDay() != TimeOfDay.INAPPLICABLE ? leaveRequest.getTimeOfDay() : TimeOfDay.INAPPLICABLE)
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

        employeeLeaveRepository.save(leave);
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

        if ( currentUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException("You are not authorized to treat this leave request.");
        }

        if (leaveRequest.getLeaveType() == LeaveType.PERSONAL_LEAVE && userRequestingLeave.getLeaveDays() < Duration.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()).toDays()) {
            leaveRequest.setStatus(Status.REJECTED);
            throw new IllegalStateException("User does not have enough leave balance.");
        }

        // Consider additional conditions or business rules for setting the status
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            leaveRequest.setStatus(Status.ACCEPTED);
            userRequestingLeave.setOnLeave(true);
            userRequestingLeave.setReturnDate(leaveRequest.getEndDate());
            userRepository.save(userRequestingLeave);
            updateLeaveBalance(leaveRequest, userRequestingLeave);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            leaveRequest.setStatus(Status.REJECTED);
            // You may want to handle additional actions for a rejected leave request
        } else {
            throw new IllegalArgumentException("Invalid status provided.");
        }

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
        existingLeaveRequest.setLeaveType(leaveRequest.getLeaveType() == null ? existingLeaveRequest.getLeaveType() : leaveRequest.getLeaveType());
        existingLeaveRequest.setExceptionalLeaveType(leaveRequest.getExceptionalLeaveType() == null ? existingLeaveRequest.getExceptionalLeaveType() : leaveRequest.getExceptionalLeaveType());
        existingLeaveRequest.setLeaveType(leaveRequest.getLeaveType() == null ? existingLeaveRequest.getLeaveType() : leaveRequest.getLeaveType());
        existingLeaveRequest.setStartDate(leaveRequest.getStartDate() == null ? existingLeaveRequest.getStartDate() : leaveRequest.getStartDate());
        existingLeaveRequest.setEndDate(leaveRequest.getEndDate() == null ? existingLeaveRequest.getEndDate() : leaveRequest.getEndDate());

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
}
