package com.telnet.leaveapp.telnetleavemanager.exceptions;

public class InsufficientAuthorizationBalanceException  extends RuntimeException {

    public InsufficientAuthorizationBalanceException() {
        super();
    }

    public InsufficientAuthorizationBalanceException(String message) {
        super(message);
    }

    public InsufficientAuthorizationBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientAuthorizationBalanceException(Throwable cause) {
        super(cause);
    }
}