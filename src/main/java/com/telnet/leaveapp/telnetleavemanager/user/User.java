package com.telnet.leaveapp.telnetleavemanager.user;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.telnet.leaveapp.telnetleavemanager.entities.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"tokens","leaves"})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    @JsonIgnore
    private String password;
    private String phone;
    @Builder.Default
    private boolean onLeave = false;
    @Nullable
    @Builder.Default
    private LocalDateTime returnDate = null;
    @Builder.Default
    private double leaveDays = 26;
    @Builder.Default
    private int externalActivitiesLimit = 2;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean mfaEnabled;
    private String secret;

    @ManyToOne
    @JoinColumn(name = "team_id",nullable = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private Team team;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<EmployeeLeave> leaves;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "organizational_unit_id")
    private OrganizationalUnit organizationalUnit;

    @JsonIgnore
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Token> tokens;

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<ExternalAuthorization> externalAuthorizations;
//

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
