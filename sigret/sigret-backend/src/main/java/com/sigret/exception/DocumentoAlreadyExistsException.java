package com.sigret.exception;

public class DocumentoAlreadyExistsException extends RuntimeException {
    public DocumentoAlreadyExistsException(String message) {
        super(message);
    }
}
