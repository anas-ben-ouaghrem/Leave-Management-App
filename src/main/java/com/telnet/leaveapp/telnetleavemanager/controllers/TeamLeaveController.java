package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamLeaveRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamLeave;
import com.telnet.leaveapp.telnetleavemanager.services.TeamLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team-leave")
@RequiredArgsConstructor
public class TeamLeaveController {

    private final TeamLeaveService teamLeaveService;

    @PostMapping("/management/create")
    public ResponseEntity<TeamLeave> createTeamLeave(
            @RequestParam String email,
            @RequestBody TeamLeaveRequest request
            ) {
        TeamLeave createdTeamLeave = teamLeaveService.createTeamLeave(email,request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeamLeave);
    }

    @PutMapping("/management/treat/{id}")
    public ResponseEntity<TeamLeave> treatTeamLeaveRequest(
            @RequestParam String email,
            @PathVariable Long id,
            @RequestParam String status
    ) {
        TeamLeave teamLeave = teamLeaveService.treatTeamLeaveRequest(email, id, status);
        return new ResponseEntity<>(teamLeave, HttpStatus.OK);
    }

    @GetMapping("/management/all")
    public ResponseEntity<List<TeamLeave>> getAllTeamLeaves() {
        List<TeamLeave> teamLeaves = teamLeaveService.getAllTeamLeaves();
        return new ResponseEntity<>(teamLeaves, HttpStatus.OK);
    }

    @GetMapping("/management/get/{id}")
    public ResponseEntity<TeamLeave> getTeamLeaveById(@PathVariable Long id) {
        TeamLeave teamLeave = teamLeaveService.getTeamLeaveById(id);
        return new ResponseEntity<>(teamLeave, HttpStatus.OK);
    }

    @DeleteMapping("/management/delete/{id}")
    public ResponseEntity<Void> deleteTeamLeave(@PathVariable Long id) {
        teamLeaveService.deleteTeamLeave(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/management/for-team/{teamId}")
    public ResponseEntity<List<TeamLeave>> getTeamLeavesForTeam(@PathVariable Long teamId) {
        List<TeamLeave> teamLeaves = teamLeaveService.getTeamLeavesForTeam(teamId);
        return new ResponseEntity<>(teamLeaves, HttpStatus.OK);
    }

    @DeleteMapping("/management/delete/team-lead/{teamLeaveId}")
    public ResponseEntity<Void> deleteTeamLeaveRequest(@PathVariable Long teamLeaveId) {
        teamLeaveService.deleteLeaveRequestTeamLead(teamLeaveId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PutMapping("/management/update/employee/{teamLeaveId}")
    public ResponseEntity<TeamLeave> updateTeamLeaveRequest(@PathVariable Long teamLeaveId) {
        TeamLeave teamLeave = teamLeaveService.updateLeaveRequestEmployee(teamLeaveId);
        return new ResponseEntity<>(teamLeave, HttpStatus.OK);
    }

    @PutMapping("/management/update/{teamLeaveId}")
    public ResponseEntity<TeamLeave> updateTeamLeave(@PathVariable Long teamLeaveId, @RequestBody TeamLeaveRequest request) {
        TeamLeave teamLeave = teamLeaveService.updateTeamLeave(teamLeaveId, request);
        return new ResponseEntity<>(teamLeave, HttpStatus.OK);
    }

}