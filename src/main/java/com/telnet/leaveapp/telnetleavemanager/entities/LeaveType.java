package com.telnet.leaveapp.telnetleavemanager.entities;

import lombok.Getter;

@Getter
public enum LeaveType {
    PERSONAL_LEAVE("Personal Leave", 2),
    EXCEPTIONAL_LEAVE("Exceptional leave",2),
    SICK_LEAVE("Sick Leave", 10),
    HALF_DAY("Unpaid Leave", 0);


    private final String typeName;
    private final int duration;

    LeaveType(String typeName, int duration) {
        this.typeName = typeName;
        this.duration = duration;
    }

}