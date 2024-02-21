package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.repositories.OrganizationalUnitRepository;
import com.telnet.leaveapp.telnetleavemanager.services.TeamService;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @PostMapping("/management/create")
    public ResponseEntity<Void> createTeam(
            @RequestBody TeamRequest request
    ) {
            teamService.createTeam(request);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @GetMapping("/management/all")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.status(HttpStatus.OK).body(teams);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Integer id) {
        Team team = teamService.getTeamById(id);
        return new ResponseEntity<>(team, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Integer id) {
        teamService.deleteTeam(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/delete/by-name/{teamName}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String teamName) {
        teamService.deleteTeamByName(teamName);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/members/{teamId}")
    public ResponseEntity<List<User>> getMembersOfTeam(@PathVariable Integer teamId) {
        List<User> members = teamService.getMembersOfTeam(teamId);
        return ResponseEntity.status(HttpStatus.OK).body(members);
    }

    @GetMapping("/management/for-organizational-unit/{organizationalUnitId}")
    public ResponseEntity<Set<Team>> getTeamsForOrganizationalUnit(@PathVariable Long organizationalUnitId) {
        Set<Team> teams = teamService.getTeamsForOrganizationalUnit(organizationalUnitId);
        return ResponseEntity.status(HttpStatus.OK).body(teams);
    }

    @GetMapping("/management/{teamName}")
    public ResponseEntity<Team> getTeamByName(@PathVariable String teamName) {
        Team team = teamService.getTeamByName(teamName);
        return ResponseEntity.status(HttpStatus.OK).body(team);
    }

    @PutMapping("/management/update/{teamId}")
    public ResponseEntity<Void> updateTeam(@PathVariable Integer teamId, @RequestBody TeamRequest request) {
        teamService.updateTeam(teamId, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/management/for-manager/{managerEmail}")
    public ResponseEntity<List<Team>> getTeamsByManager(@PathVariable String managerEmail){
        List<Team> teams = teamService.getTeamsByManager(managerEmail);
        return ResponseEntity.status(HttpStatus.OK).body(teams);
    }

}