package com.telnet.leaveapp.telnetleavemanager.entities;

import lombok.Getter;

@Getter
public enum LeaveDuration {
    OneHour("One Hour", 60),
    TwoHours("Two Hours", 120),
    ThirtyMinutes("Thirty Minutes",30),
    NinetyMinutes("Ninety Minutes",90);

    private final String durationName;
    private final int duration;

    LeaveDuration(String typeName, int duration) {
        this.durationName = typeName;
        this.duration = duration;
    }
}
