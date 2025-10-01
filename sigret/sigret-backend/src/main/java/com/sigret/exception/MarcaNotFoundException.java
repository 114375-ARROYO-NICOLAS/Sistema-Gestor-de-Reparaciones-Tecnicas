package com.sigret.exception;

public class MarcaNotFoundException extends RuntimeException {
    public MarcaNotFoundException(String message) {
        super(message);
    }
}
