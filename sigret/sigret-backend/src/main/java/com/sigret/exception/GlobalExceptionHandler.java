package com.sigret.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Credenciales inválidas");
        error.put("message", "Usuario o contraseña incorrectos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("error", "Errores de validación");
        response.put("details", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNotFound(UsuarioNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Usuario no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyExists(UsernameAlreadyExistsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Username ya existe");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmpleadoAlreadyHasUserException.class)
    public ResponseEntity<Map<String, String>> handleEmpleadoAlreadyHasUser(EmpleadoAlreadyHasUserException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Empleado ya tiene usuario");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleClienteNotFound(ClienteNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Cliente no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DocumentoAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleDocumentoAlreadyExists(DocumentoAlreadyExistsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Documento ya existe");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EquipoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEquipoNotFound(EquipoNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Equipo no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NumeroSerieAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleNumeroSerieAlreadyExists(NumeroSerieAlreadyExistsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Número de serie ya existe");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MarcaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMarcaNotFound(MarcaNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Marca no encontrada");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MarcaAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleMarcaAlreadyExists(MarcaAlreadyExistsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Marca ya existe");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ModeloNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleModeloNotFound(ModeloNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Modelo no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ModeloAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleModeloAlreadyExists(ModeloAlreadyExistsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Modelo ya existe");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(OrdenTrabajoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrdenTrabajoNotFound(OrdenTrabajoNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Orden de trabajo no encontrada");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PresupuestoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePresupuestoNotFound(PresupuestoNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Presupuesto no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ServicioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleServicioNotFound(ServicioNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Servicio no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmpleadoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEmpleadoNotFound(EmpleadoNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Empleado no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TipoPersonaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTipoPersonaNotFound(TipoPersonaNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Tipo de persona no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TipoEmpleadoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTipoEmpleadoNotFound(TipoEmpleadoNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Tipo de empleado no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TipoDocumentoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTipoDocumentoNotFound(TipoDocumentoNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Tipo de documento no encontrado");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TipoAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleTipoAlreadyExists(TipoAlreadyExistsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Tipo ya existe");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
