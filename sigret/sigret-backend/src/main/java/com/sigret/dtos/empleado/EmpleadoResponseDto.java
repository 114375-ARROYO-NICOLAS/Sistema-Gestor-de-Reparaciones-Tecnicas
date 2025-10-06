package com.sigret.dtos.empleado;

import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponseDto {

    private Long id;
    private String nombreCompleto;
    private String nombre;
    private String apellido;
    private String razonSocial;
    private String documento;
    private String tipoDocumento;
    private String tipoPersona;
    private String sexo;
    private String tipoEmpleado;
    private Long tipoEmpleadoId;
    private Boolean activo;
    
    // Información del usuario asociado
    private Long usuarioId;
    private String username;
    private RolUsuario rolUsuario;
    private Boolean usuarioActivo;
    private LocalDateTime fechaCreacionUsuario;
    private LocalDateTime ultimoLogin;
    
    // Direcciones de la persona
    private List<DireccionListDto> direcciones;
}

