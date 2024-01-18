package com.telnet.leaveapp.telnetleavemanager.controllers;

import com.telnet.leaveapp.telnetleavemanager.dto.ExternalAuthorizationRequest;
import com.telnet.leaveapp.telnetleavemanager.entities.ExternalAuthorization;
import com.telnet.leaveapp.telnetleavemanager.entities.Status;
import com.telnet.leaveapp.telnetleavemanager.services.ExternalAuthorizationService;
import com.telnet.leaveapp.telnetleavemanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/external-authorization")
@RequiredArgsConstructor
public class ExternalAuthorizationController {


    private final ExternalAuthorizationService externalAuthorizationService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<ExternalAuthorization> createExternalAuthorization(
            @RequestParam String currentUserEmail,
            @RequestBody ExternalAuthorizationRequest request
            ) {

        ExternalAuthorization createdAuthorization = externalAuthorizationService.createExternalAuthorization(currentUserEmail,request);
        return new ResponseEntity<>(createdAuthorization, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExternalAuthorization>> getAllExternalAuthorizations() {
        List<ExternalAuthorization> authorizations = externalAuthorizationService.getAllExternalAuthorizations();
        return new ResponseEntity<>(authorizations, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ExternalAuthorization> getExternalAuthorizationById(@PathVariable Long id) {
        ExternalAuthorization authorization = externalAuthorizationService.getExternalAuthorizationById(id);
        return new ResponseEntity<>(authorization, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExternalAuthorization(@PathVariable Long id) {
        externalAuthorizationService.deleteExternalAuthorization(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/treat/{id}")
    public ResponseEntity<Void> treatExternalAuthorization(@PathVariable Long id, @RequestParam String status, @RequestParam String currentUserEmail) {
        externalAuthorizationService.treatExternalAuthorization(id, Status.valueOf(status), currentUserEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/manager/get-by-manager/{currentUserEmail}")
    public ResponseEntity<List<ExternalAuthorization>> getExternalAuthorizationsByManager(@PathVariable String currentUserEmail) {
        List<ExternalAuthorization> authorizations = externalAuthorizationService.getExternalAuthorizationsByTeamManager(currentUserEmail);
        return new ResponseEntity<>(authorizations, HttpStatus.OK);
    }

    @GetMapping("/user/get-by-user/{currentUserEmail}")
    public ResponseEntity<List<ExternalAuthorization>> getExternalAuthorizationsByUser(@PathVariable String currentUserEmail) {
        List<ExternalAuthorization> authorizations = externalAuthorizationService.getExternalAuthorizationsByUser(currentUserEmail);
        return new ResponseEntity<>(authorizations, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ExternalAuthorization> updateExternalAuthorization(@PathVariable Long id, @RequestBody ExternalAuthorizationRequest request) {
        ExternalAuthorization updatedAuthorization = externalAuthorizationService.updateExternalAuthorization(id,request);
        return new ResponseEntity<>(updatedAuthorization, HttpStatus.OK);
    }

}
