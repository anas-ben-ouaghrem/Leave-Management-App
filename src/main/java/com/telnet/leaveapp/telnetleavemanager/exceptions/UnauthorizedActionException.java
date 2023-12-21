package com.telnet.leaveapp.telnetleavemanager.exceptions;

public class UnauthorizedActionException extends RuntimeException{
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
