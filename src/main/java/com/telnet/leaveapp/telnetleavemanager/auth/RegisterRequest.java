package com.telnet.leaveapp.telnetleavemanager.auth;

import com.telnet.leaveapp.telnetleavemanager.user.Gender;
import com.telnet.leaveapp.telnetleavemanager.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private Gender gender;
    private String email;
    private String password;
    private Role role;
    private String phone;
    @Builder.Default
    private boolean mfaEnabled = false;
}
