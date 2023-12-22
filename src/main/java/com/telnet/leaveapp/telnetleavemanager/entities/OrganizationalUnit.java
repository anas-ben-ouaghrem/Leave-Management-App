package com.telnet.leaveapp.telnetleavemanager.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OrganizationalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime createdAt;
    private String unitName;

    @OneToMany(mappedBy = "organizationalUnit",  cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private Set<Team> teams;

    @OneToMany(mappedBy = "organizationalUnit", cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = false)
    private Set<User> members;

    @ManyToOne
    private User manager;

}
