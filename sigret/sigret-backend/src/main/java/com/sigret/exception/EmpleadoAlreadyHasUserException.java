package com.sigret.exception;

public class EmpleadoAlreadyHasUserException extends RuntimeException {
    public EmpleadoAlreadyHasUserException(String message) {
        super(message);
    }
}
