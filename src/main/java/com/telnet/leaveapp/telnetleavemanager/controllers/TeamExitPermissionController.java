package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamExitPermissionRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamExitPermission;
import com.telnet.leaveapp.telnetleavemanager.services.ExternalAuthorizationService;
import com.telnet.leaveapp.telnetleavemanager.services.TeamExitPermissionService;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-exit-permission")
public class TeamExitPermissionController {

    private final TeamExitPermissionService teamExitPermissionService;

    @PostMapping("/create")
    public ResponseEntity<TeamExitPermission> createTeamExitPermission(
            @RequestParam String currentUserEmail,
            @RequestBody TeamExitPermissionRequest request
    ) {

        TeamExitPermission createdAuthorization = teamExitPermissionService.createTeamExitPermission(currentUserEmail,request);
        return new ResponseEntity<>(createdAuthorization, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TeamExitPermission>> getAllTeamExitPermissions() {
        List<TeamExitPermission> teamExitPermissions = teamExitPermissionService.getAllTeamExitPermissions();
        return new ResponseEntity<>(teamExitPermissions, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<TeamExitPermission> getTeamExitPermissionById(@PathVariable Long id) {
        TeamExitPermission authorization = teamExitPermissionService.getTeamExitPermissionById(id);
        return new ResponseEntity<>(authorization, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTeamExitPermission(@PathVariable Long id) {
        teamExitPermissionService.deleteExitPermission(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/treat/{id}")
    public ResponseEntity<Void> treatTeamExitPermissionRequest(@PathVariable Long id, @RequestParam String status, @RequestParam String currentUserEmail) {
        teamExitPermissionService.treatTeamExitPermission(id, Status.valueOf(status), currentUserEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/manager/get-by-manager/{currentUserEmail}")
    public ResponseEntity<List<TeamExitPermission>> getTeamExitPermissionsByTeamManager(@PathVariable String currentUserEmail) {
        List<TeamExitPermission> authorizations = teamExitPermissionService.getTeamExitPermissionsByTeamManager(currentUserEmail);
        return new ResponseEntity<>(authorizations, HttpStatus.OK);
    }

    @GetMapping("/get-by-team/{teamName}")
    public ResponseEntity<List<TeamExitPermission>> getTeamExitPermissionsByTeam(@PathVariable String teamName) {
        List<TeamExitPermission> authorizations = teamExitPermissionService.getTeamExitPermissionsByTeam(teamName);
        return new ResponseEntity<>(authorizations, HttpStatus.OK);
    }

    @GetMapping("/user/get-by-user/{currentUserEmail}")
    public ResponseEntity<List<TeamExitPermission>> getTeamExitPermissionsByUser(@PathVariable String currentUserEmail) {
        List<TeamExitPermission> authorizations = teamExitPermissionService.getTeamExitPermissionsByUser(currentUserEmail);
        return new ResponseEntity<>(authorizations, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TeamExitPermission> updateTeamExitPermission(@PathVariable Long id, @RequestBody TeamExitPermissionRequest request) {
        TeamExitPermission updatedAuthorization = teamExitPermissionService.updateTeamExitPermission(id,request);
        return new ResponseEntity<>(updatedAuthorization, HttpStatus.OK);
    }
}
