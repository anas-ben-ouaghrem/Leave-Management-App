package com.telnet.leaveapp.telnetleavemanager.entities;

import com.telnet.leaveapp.telnetleavemanager.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    @ManyToOne
    private User manager;
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<User> members;
    @ManyToOne
    @JoinColumn(name = "organizational_unit_id",nullable = true)
    private OrganizationalUnit organizationalUnit;
}
