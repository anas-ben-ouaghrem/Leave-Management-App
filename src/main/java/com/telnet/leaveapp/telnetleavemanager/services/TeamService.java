package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.TeamRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.OrganizationalUnit;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.repositories.OrganizationalUnitRepository;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @Transactional
    public void createTeam(TeamRequest request) {
        User manager = userRepository.findByEmail(request.getTeamLeadEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (manager.getRole() == Role.USER ) {
            throw new IllegalStateException("User set for manager is not a manager nor an admin");
        }

        List<User> members = new ArrayList<>();
        if(request.getTeamMembersEmails() != null) {
            members =  Arrays.stream(request.getTeamMembersEmails())
                    .map(userRepository::findByEmail)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }

        OrganizationalUnit organizationalUnit = null;
        if (request.getOrganizationalUnitName() != null) {
            organizationalUnit = organizationalUnitRepository.findByName(request.getOrganizationalUnitName())
                    .orElseThrow(() -> new RuntimeException("Organizational Unit Not Found!"));
        }

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .manager(manager)
                .members(members)
                .createdAt(LocalDateTime.now())
                .organizationalUnit(organizationalUnit)
                .build();
        teamRepository.saveAndFlush(team);
        manager.setTeam(team);
        userRepository.saveAndFlush(manager);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Integer id) {
        return teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public Team getTeamByName(String name) {
        return teamRepository.findByName(name).orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public void deleteTeam(Integer id) {
        teamRepository.deleteById(id);
    }

    public Set<Team> getTeamsForOrganizationalUnit(Long organizationalUnitId) {
        OrganizationalUnit organizationalUnit = organizationalUnitRepository.findById(organizationalUnitId).orElseThrow(() -> new RuntimeException("Organizational unit not found"));
        return organizationalUnit.getTeams();
    }

    public void deleteTeamByName(String name) {
        Team team = teamRepository.findByName(name)
                        .orElseThrow(()-> new RuntimeException("Team with name " + name + "not found"));
        teamRepository.delete(team);
    }
    public List<User> getMembersOfTeam(Integer teamId) {
        Team team = getTeamById(teamId);
        return team != null ? team.getMembers() : new ArrayList<>();
    }

    public void updateTeam(Integer teamId, TeamRequest request) {
        System.out.println(request);
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        User manager = userRepository.findByEmail(request.getTeamLeadEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (manager.getRole() == Role.USER ) {
            throw new IllegalStateException("User set for manager is not a manager nor an admin");
        }

        List<User> members = new ArrayList<>();
        if(request.getTeamMembersEmails() != null) {
            members =  Arrays.stream(request.getTeamMembersEmails())
                    .map(userRepository::findByEmail)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }

        OrganizationalUnit organizationalUnit = null;
        if (request.getOrganizationalUnitName() != null) {
            organizationalUnit = organizationalUnitRepository.findByName(request.getOrganizationalUnitName())
                    .orElseThrow(() -> new RuntimeException("Organizational Unit Not Found!"));
        }

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setManager(manager);
        team.setMembers(members);
        team.setOrganizationalUnit(organizationalUnit);
        teamRepository.saveAndFlush(team);
        manager.setTeam(team);
        userRepository.saveAndFlush(manager);
    }
}



