package com.telnet.leaveapp.telnetleavemanager.entities;

import lombok.Getter;

@Getter
public enum ExceptionalLeaveType {

    MATERNITY_LEAVE("Maternity Leave", 40),
    PATERNITY_LEAVE("Paternity Leave", 5),
    PARENT_DEATH_LEAVE("Parent Death Leave", 3),
    MARRIAGE_LEAVE("Marriage Leave", 3),
    BEREAVEMENT_LEAVE("Bereavement Leave", 3),
    CHILD_BIRTH_LEAVE("Child Birth Leave", 3),
    CHILD_MARRIAGE_LEAVE("Child Marriage Leave", 3),
    CHILD_DEATH_LEAVE("Child Death Leave", 3),
    NONE("None", 0 );

    private final String typeName;
    private final int duration;

    ExceptionalLeaveType(String typeName, int duration) {
        this.typeName = typeName;
        this.duration = duration;
    }
}
