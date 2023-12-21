package com.telnet.leaveapp.telnetleavemanager.repositories;

import com.telnet.leaveapp.telnetleavemanager.entities.OrganizationalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, Long> {
    Optional<OrganizationalUnit> findByUnitName(String unitName);

    void deleteByUnitName(String unitName);
}
