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
public class UsuarioResponseDto {

    private Long id;
    private String username;
    private String nombreCompleto;
    private Long empleadoId;
    private String empleadoNombre;
    private RolUsuario rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;

}
