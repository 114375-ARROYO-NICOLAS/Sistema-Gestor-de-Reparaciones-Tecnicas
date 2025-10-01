package com.sigret.exception;

public class ServicioNotFoundException extends RuntimeException {
    public ServicioNotFoundException(String message) {
        super(message);
    }
}
