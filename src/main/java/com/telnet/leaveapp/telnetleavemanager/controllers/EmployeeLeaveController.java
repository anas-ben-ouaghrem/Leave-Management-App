package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.LeaveRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.EmployeeLeave;
import com.telnet.leaveapp.telnetleavemanager.services.EmployeeLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-leave")
@RequiredArgsConstructor
public class EmployeeLeaveController {

    private final EmployeeLeaveService employeeLeaveService;

    @PostMapping("/user/create")
    public ResponseEntity<Void> createLeaveRequest(String currentUserEmail, @RequestBody LeaveRequest leaveRequest) {
        employeeLeaveService.createLeaveRequest(currentUserEmail,leaveRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/manager/treat/")
    public ResponseEntity<EmployeeLeave> treatLeaveRequest(
            @RequestParam String currentUserEmail,
            @RequestParam Long leaveRequestId,
            @RequestParam String status
    ) {
        EmployeeLeave treatedLeave = employeeLeaveService.treatLeaveRequest(currentUserEmail, leaveRequestId, status);
        return new ResponseEntity<>(treatedLeave, HttpStatus.OK);
    }

    @PutMapping("/update/{leaveRequestId}")
    public ResponseEntity<EmployeeLeave> updateLeaveRequest(@PathVariable Long leaveRequestId, @RequestBody LeaveRequest leaveRequest) {
        EmployeeLeave updatedLeave = employeeLeaveService.updateLeaveRequest(leaveRequestId,leaveRequest);
        return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{leaveRequestId}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Integer leaveRequestId) {
        employeeLeaveService.deleteLeaveRequest(leaveRequestId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get/{leaveRequestId}")
    public ResponseEntity<EmployeeLeave> getLeaveRequestById(@PathVariable Long leaveRequestId) {
        EmployeeLeave leaveRequest = employeeLeaveService.getLeaveRequestById(leaveRequestId);
        return new ResponseEntity<>(leaveRequest, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeLeave>> getAllLeaveRequests() {
        List<EmployeeLeave> leaveRequests = employeeLeaveService.getAllLeaveRequests();
        return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmployeeLeave>> getLeaveRequestsByUserId(@PathVariable Integer userId) {
        List<EmployeeLeave> leaveRequests = employeeLeaveService.getLeaveRequestsByUserId(userId);
        return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
    }

}
