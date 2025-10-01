package com.sigret.entities;

import com.sigret.enums.RolUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false, unique = true)
    private Empleado empleado;

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede exceder 50 caracteres")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255, message = "La contraseña no puede exceder 255 caracteres")
    @Column(name = "password", nullable = false, length = 255)
    private String password; // Encriptado con BCrypt

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;


    // Métodos de utilidad
    public String getNombreCompleto() {
        return empleado != null ? empleado.getNombreCompleto() : username;
    }

    public boolean esActivo() {
        return activo && empleado != null && empleado.getActivo();
    }

    public boolean tienePropietario() {
        return rol == RolUsuario.PROPIETARIO;
    }

    public boolean tieneAdministrativo() {
        return rol == RolUsuario.ADMINISTRATIVO || rol == RolUsuario.PROPIETARIO;
    }

    public boolean tieneTecnico() {
        return rol != null; // Todos los roles pueden hacer tareas técnicas básicas
    }
}
