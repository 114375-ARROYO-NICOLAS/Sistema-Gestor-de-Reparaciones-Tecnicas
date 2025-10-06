package com.sigret.dtos.empleado;

import com.sigret.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoListDto {

    private Long id;
    private String nombreCompleto;
    private String documento;
    private String tipoEmpleado;
    private Boolean activo;
    private Long usuarioId;
    private String username;
    private RolUsuario rolUsuario;
    private Boolean usuarioActivo;
}

