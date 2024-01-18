package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.OrganizationalUnitRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.OrganizationalUnit;
import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import com.telnet.leaveapp.telnetleavemanager.services.OrganizationalUnitService;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizational-unit")
@RequiredArgsConstructor
public class OrganizationalUnitController {

    private final OrganizationalUnitService organizationalUnitService;
    private final UserRepository userRepository;

    @PostMapping("/admin/create")
    public ResponseEntity<OrganizationalUnit> createOrganizationalUnit(
            @RequestParam String email,
            @RequestBody OrganizationalUnitRequest request
    ) {
        OrganizationalUnit createdOrganizationalUnit = organizationalUnitService.createOrganizationalUnit(email,request);

        return new ResponseEntity<>(createdOrganizationalUnit, HttpStatus.CREATED);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<java.util.List<OrganizationalUnit>> getAllOrganizationalUnits() {
        java.util.List<OrganizationalUnit> organizationalUnits = organizationalUnitService.getAllOrganizationalUnits();
        return new ResponseEntity<>(organizationalUnits, HttpStatus.OK);
    }

    @GetMapping("/admin/get/{id}")
    public ResponseEntity<OrganizationalUnit> getOrganizationalUnitById(@PathVariable Long id) {
        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnitById(id);
        return new ResponseEntity<>(organizationalUnit, HttpStatus.OK);
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<Void> deleteOrganizationalUnit(@PathVariable Long id) {
        organizationalUnitService.deleteOrganizationalUnit(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

   @PutMapping("/admin/affect-team")
    public void affectTeamToOrganizationalUnit(@RequestParam Long organizationalUnitId, @RequestParam String teamName) {
        organizationalUnitService.affectTeamToOrganizationalUnit(organizationalUnitId, teamName);
    }

    @PutMapping("/admin/affect-member")
    public void affectMemberToOrganizationalUnit(@RequestParam Long organizationalUnitId, @RequestParam String memberEmail) {
        organizationalUnitService.affectMemberToOrganizationalUnit(organizationalUnitId, memberEmail);
    }

    @PutMapping("/admin/remove-team")
    public void removeTeamFromOrganizationalUnit(@RequestParam Long organizationalUnitId, @RequestParam String teamName) {
        organizationalUnitService.removeTeamFromOrganizationalUnit(organizationalUnitId, teamName);
    }

    @PutMapping("/admin/remove-member")
    public void removeMemberFromOrganizationalUnit(@RequestParam Long organizationalUnitId, @RequestParam String memberEmail) {
        organizationalUnitService.removeMemberFromOrganizationalUnit(organizationalUnitId, memberEmail);
    }

    @PutMapping("/admin/affect-manager")
    public void affectManagerToOrganizationalUnit(@RequestParam Long organizationalUnitId, @RequestParam String managerEmail) {
        organizationalUnitService.affectManagerToOrganizationalUnit(organizationalUnitId, userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    @DeleteMapping("/admin/delete-by-name/{unitName}")
    public ResponseEntity<Void> deleteOrganizationalUnit(@PathVariable String unitName) {
        organizationalUnitService.deleteOrganizationalUnitByName(unitName);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/admin/teams/{organizationalUnitId}")
    public ResponseEntity<java.util.Set<Team>> getTeamsOfOrganizationalUnit(@PathVariable Long organizationalUnitId) {
        java.util.Set<Team> teams = organizationalUnitService.getTeamsOfOrganizationalUnit(organizationalUnitId);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<OrganizationalUnit> updateOrganizationalUnit(@PathVariable Long id, @RequestBody OrganizationalUnitRequest request) {
        OrganizationalUnit updatedOrganizationalUnit = organizationalUnitService.updateOrganizationalUnit(id,request);
        return new ResponseEntity<>(updatedOrganizationalUnit, HttpStatus.OK);
    }
}