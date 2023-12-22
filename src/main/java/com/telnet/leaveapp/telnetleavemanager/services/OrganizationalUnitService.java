package com.telnet.leaveapp.telnetleavemanager.services;

import com.telnet.leaveapp.telnetleavemanager.dto.OrganizationalUnitRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.OrganizationalUnit;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.exceptions.UnauthorizedActionException;
import com.telnet.leaveapp.telnetleavemanager.repositories.OrganizationalUnitRepository;
import com.telnet.leaveapp.telnetleavemanager.repositories.TeamRepository;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationalUnitService {

    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public OrganizationalUnit createOrganizationalUnit(String currentUserEmail, OrganizationalUnitRequest request) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != com.telnet.leaveapp.telnetleavemanager.user.Role.ADMIN) {
            throw new UnauthorizedActionException("You are not authorized to create an organizational unit");
        }

        OrganizationalUnit organizationalUnit = new OrganizationalUnit();
        organizationalUnit.setUnitName(request.getUnitName());
        organizationalUnit.setManager(userRepository.findByEmail(request.getManagerEmail()).orElseThrow(() -> new RuntimeException("User not found")));
        if(request.getTeamNames() != null) {
            organizationalUnit.setTeams(request.getTeamNames().stream()
                    .map(teamName -> Team.builder()
                            .name(teamName)
                            .build())
                    .collect(java.util.stream.Collectors.toSet()));
        } else {
            organizationalUnit.setTeams(new HashSet<>());
        }
        organizationalUnit.setCreatedAt(LocalDateTime.now());
        if (request.getMemberEmails() != null) {
            organizationalUnit.setMembers(request.getMemberEmails().stream()
                    .map(memberEmail -> userRepository.findByEmail(memberEmail).orElseThrow(() -> new RuntimeException("User not found")))
                    .collect(java.util.stream.Collectors.toSet()));
        } else {
            organizationalUnit.setMembers(new HashSet<>());
        }

        return organizationalUnitRepository.save(organizationalUnit);
    }

    public List<OrganizationalUnit> getAllOrganizationalUnits() {
        return organizationalUnitRepository.findAll();
    }

    public OrganizationalUnit getOrganizationalUnitById(Long id) {
        return organizationalUnitRepository.findById(id).orElse(null);
    }

    public void deleteOrganizationalUnit(Long id) {
        organizationalUnitRepository.deleteById(id);
    }

    public OrganizationalUnit getOrganizationalUnitByName(String unitName) {
        return organizationalUnitRepository.findByUnitName(unitName).orElseThrow(() -> new RuntimeException("Organizational unit not found"));
    }
    public Set<Team> getTeamsOfOrganizationalUnit(Long organizationalUnitId) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitById(organizationalUnitId);
        return organizationalUnit != null ? organizationalUnit.getTeams() : new HashSet<>();
    }

    public void deleteOrganizationalUnitByName(String unitName) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitByName(unitName);
        organizationalUnitRepository.delete(organizationalUnit);
    }

    public void affectTeamToOrganizationalUnit(Long organizationalUnitId, String teamName) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitById(organizationalUnitId);
        if (organizationalUnit != null) {
            Team team = teamRepository.findByName(teamName).orElseThrow(() -> new RuntimeException("Team not found"));
            log.info(team.toString());
            organizationalUnit.getTeams().add(team);
            team.setOrganizationalUnit(organizationalUnit);
            teamRepository.saveAndFlush(team);
            organizationalUnitRepository.saveAndFlush(organizationalUnit);
            log.info("Team " + teamName + " added to organizational unit");
        }
    }

    public void removeTeamFromOrganizationalUnit(Long organizationalUnitId, String teamName) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitById(organizationalUnitId);
        if (organizationalUnit != null) {
            Team team = teamRepository.findByName(teamName).orElseThrow(() -> new RuntimeException("Team not found"));
            log.info(team.toString());
            organizationalUnit.getTeams().remove(team);
            team.setOrganizationalUnit(null);
            teamRepository.saveAndFlush(team);
            organizationalUnitRepository.saveAndFlush(organizationalUnit);
            log.info("Team " + teamName + " removed from organizational unit");
        }
    }

    public void affectManagerToOrganizationalUnit(Long organizationalUnitId, User manager) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitById(organizationalUnitId);
        if (organizationalUnit != null) {
            organizationalUnit.setManager(manager);
            organizationalUnitRepository.save(organizationalUnit);
        }
    }

    @Transactional
    public void affectMemberToOrganizationalUnit(Long organizationalUnitId, String memberEmail) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitById(organizationalUnitId);

        if (organizationalUnit != null) {
            User member = userRepository.findByEmail(memberEmail).orElseThrow(() -> new RuntimeException("User not found"));
            Set<User> members = organizationalUnit.getMembers();

            // Avoiding cyclic dependency
            if (!members.contains(member)) {
                members.add(member);
                member.setOrganizationalUnit(organizationalUnit);
                userRepository.save(member);
                organizationalUnit.setMembers(members);
                organizationalUnitRepository.saveAndFlush(organizationalUnit);
            }
        }
    }

    public void removeMemberFromOrganizationalUnit(Long organizationalUnitId, String memberEmail) {
        OrganizationalUnit organizationalUnit = getOrganizationalUnitById(organizationalUnitId);
        if (organizationalUnit != null) {
            User member = userRepository.findByEmail(memberEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Set<User> members = organizationalUnit.getMembers();

            // Avoiding cyclic dependency
            if (members.contains(member)) {
                members.remove(member);
                member.setOrganizationalUnit(null);
                userRepository.saveAndFlush(member);
                organizationalUnit.setMembers(members);
                organizationalUnitRepository.saveAndFlush(organizationalUnit);
                log.info("Member " + memberEmail + " removed from organizational unit");
            }
        }
    }
}
