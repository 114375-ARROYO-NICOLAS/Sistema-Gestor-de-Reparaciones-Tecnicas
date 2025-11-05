package com.sigret.exception;

public class UsuarioSinEmpleadoException extends RuntimeException {
    public UsuarioSinEmpleadoException(String message) {
        super(message);
    }
}
