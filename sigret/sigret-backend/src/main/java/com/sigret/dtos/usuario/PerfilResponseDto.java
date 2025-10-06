package com.sigret.dtos.usuario;

import com.sigret.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponseDto {

    private Long usuarioId;
    private String username;
    private RolUsuario rol;
    private Boolean usuarioActivo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;
    
    // Información del empleado
    private Long empleadoId;
    private String nombreCompleto;
    private String nombre;
    private String apellido;
    private String documento;
    private String tipoDocumento;
    private String sexo;
    private String tipoEmpleado;
    private Boolean empleadoActivo;
}

