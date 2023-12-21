package com.telnet.leaveapp.telnetleavemanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamRequest {

    private String name;
    private String description;
    private String teamLeadEmail;
    private String[] teamMembersEmails;
    private String organizationalUnitName;
}
