package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    List<EmployeeLeave> findAllByUser_Id(Integer id);
}
