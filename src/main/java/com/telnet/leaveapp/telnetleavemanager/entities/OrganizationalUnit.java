package com.telnet.leaveapp.telnetleavemanager.entities;

import com.telnet.leaveapp.telnetleavemanager.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime createdAt;
    private String unitName;

    @OneToMany(mappedBy = "organizationalUnit",  cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<Team> teams;

    @OneToMany(mappedBy = "organizationalUnit", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<User> members;

    @ManyToOne
    private User manager;

}
