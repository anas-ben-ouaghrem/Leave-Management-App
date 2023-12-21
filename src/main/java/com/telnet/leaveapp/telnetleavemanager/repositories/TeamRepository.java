package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    Optional<Team> findByName(String teamName);

    void deleteByName(String teamName);
}
