package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.ExternalAuthorization;
import com.telnet.leaveapp.telnetleavemanager.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalAuthorizationRepository extends JpaRepository<ExternalAuthorization, Long> {
    List<ExternalAuthorization> findByUserId(Integer userId);

    List<ExternalAuthorization> findByUser_Team_Manager(User manager);

    List<ExternalAuthorization> findByUser(User user);
}
