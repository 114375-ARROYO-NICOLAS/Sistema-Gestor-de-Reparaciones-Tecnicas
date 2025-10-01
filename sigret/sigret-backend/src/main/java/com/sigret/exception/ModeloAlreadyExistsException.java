package com.sigret.exception;

public class ModeloAlreadyExistsException extends RuntimeException {
    public ModeloAlreadyExistsException(String message) {
        super(message);
    }
}
