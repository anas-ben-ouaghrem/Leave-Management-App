package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.ExternalAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalAuthorizationRepository extends JpaRepository<ExternalAuthorization, Long> {
    List<ExternalAuthorization> findByUserId(Integer userId);
}
