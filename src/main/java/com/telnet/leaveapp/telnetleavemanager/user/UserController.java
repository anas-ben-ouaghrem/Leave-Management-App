package com.telnet.leaveapp.telnetleavemanager.user;

import com.telnet.leaveapp.telnetleavemanager.auth.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

        @Operation(
            description = "Get endpoint for management",
            summary = "This is a summary for GET endpoint",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/admin/addUser")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            userService.addUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User Added Successfully");
        } catch (RuntimeException err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in creating User \n " + err.getMessage());
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestParam String currentUserEmail, @RequestParam String targetUserEmail, @RequestBody RegisterRequest request) {
        User updatedUser = userService.updateUser(currentUserEmail, targetUserEmail, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/admin/{email}")
    public ResponseEntity<Void> deleteUserByEmail(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();

    }

    @PostMapping("/manager/affect-team")
    public ResponseEntity<?> affectTeamToUser(@RequestParam String userEmail,@RequestParam String teamName){
        try {
            userService.affectTeamToUser(userEmail,teamName);
            return ResponseEntity.status(HttpStatus.CREATED).body("User Added Successfully to team "+ teamName);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in creating User \n " + e.getMessage());
        }
    }

    @PostMapping("/manager/remove-from-team")
    public ResponseEntity<?> removeUserFromTeam(String userEmail, String teamName){
        try {
            userService.removeUserFromTeam(userEmail,teamName);
            return ResponseEntity.status(HttpStatus.CREATED).body("User Removed Successfully from team "+ teamName);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in creating User \n " + e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestParam("email") String email, @RequestParam("newPassword") String newPassword) {
        try {
            userService.resetPassword(email, newPassword);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
}
