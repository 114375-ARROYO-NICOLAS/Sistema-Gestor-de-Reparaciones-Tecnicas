package com.sigret.dtos.contacto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoCreateDto {

    // PersonaId es opcional cuando se usa desde Empleado/Cliente (se establece automáticamente)
    // Es obligatorio solo cuando se usa el endpoint directo de contactos
    private Long personaId;

    @NotNull(message = "El tipo de contacto es obligatorio")
    private Long tipoContactoId;

    @NotBlank(message = "La descripción del contacto es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
}

