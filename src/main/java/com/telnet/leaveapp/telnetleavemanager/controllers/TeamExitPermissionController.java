package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamExitPermissionRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.entities.TeamExitPermission;
import com.telnet.leaveapp.telnetleavemanager.services.TeamExitPermissionService;
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

    @PostMapping
    public ResponseEntity<TeamExitPermission> createTeamExitPermission(@RequestBody TeamExitPermissionRequest request) {
        // Assuming you have the current user's email available in the request or from the authentication context
        String currentUserEmail = "user@example.com";
        TeamExitPermission teamExitPermission = teamExitPermissionService.createTeamExitPermission(currentUserEmail, request);
        return new ResponseEntity<>(teamExitPermission, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamExitPermission> getTeamExitPermissionById(@PathVariable Long id) {
        TeamExitPermission teamExitPermission = teamExitPermissionService.getTeamExitPermissionById(id);
        return new ResponseEntity<>(teamExitPermission, HttpStatus.OK);
    }

    @GetMapping("/external-authorizations")
    public ResponseEntity<List<TeamExitPermission>> getAllExternalAuthorizations() {
        List<TeamExitPermission> externalAuthorizations = teamExitPermissionService.getAllExternalAuthorizations();
        return new ResponseEntity<>(externalAuthorizations, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamExitPermission> updateTeamExitPermission(@PathVariable Long id, @RequestBody TeamExitPermissionRequest request) {
        TeamExitPermission updatedTeamExitPermission = teamExitPermissionService.updateTeamExitPermission(id, request);
        return new ResponseEntity<>(updatedTeamExitPermission, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExitPermission(@PathVariable Long id) {
        teamExitPermissionService.deleteExitPermission(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<List<TeamExitPermission>> getTeamExitPermissionByTeam(@PathVariable String teamName) {
        List<TeamExitPermission> teamExitPermissions = teamExitPermissionService.getTeamExitPermissionByTeam(teamName);
        return new ResponseEntity<>(teamExitPermissions, HttpStatus.OK);
    }

    @GetMapping("/team-manager/{managerEmail}")
    public ResponseEntity<List<TeamExitPermission>> getExternalAuthorizationsByTeamManager(@PathVariable String managerEmail) {
        List<TeamExitPermission> externalAuthorizations = teamExitPermissionService.getExternalAuthorizationsByTeamManager(managerEmail);
        return new ResponseEntity<>(externalAuthorizations, HttpStatus.OK);
    }

    @GetMapping("/team-id/{userId}")
    public ResponseEntity<List<TeamExitPermission>> getTeamExitPermissionByTeamId(@PathVariable Long userId) {
        List<TeamExitPermission> teamExitPermissions = teamExitPermissionService.getTeamExitPermissionByTeamId(userId);
        return new ResponseEntity<>(teamExitPermissions, HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> treatTeamExitPermission(@PathVariable Long id, @RequestParam Status status) {
        // Assuming you have the current user's email available in the request or from the authentication context
        String currentUserEmail = "user@example.com";
        teamExitPermissionService.treatTeamExitPermission(id, status, currentUserEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
