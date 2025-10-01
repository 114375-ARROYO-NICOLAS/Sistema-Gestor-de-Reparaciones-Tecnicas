package com.sigret.dtos.marca;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarcaResponseDto {

    private Long id;
    private String descripcion;
}
