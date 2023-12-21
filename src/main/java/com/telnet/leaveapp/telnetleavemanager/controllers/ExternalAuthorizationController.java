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
    public ResponseEntity<Void> treatExternalAuthorization(@PathVariable Long id, @RequestParam String status, @RequestParam String email) {
        externalAuthorizationService.treatExternalAuthorization(id, Status.valueOf(status), email);
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
