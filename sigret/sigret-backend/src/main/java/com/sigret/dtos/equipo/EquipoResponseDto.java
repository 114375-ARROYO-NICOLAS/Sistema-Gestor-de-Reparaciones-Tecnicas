package com.sigret.dtos.equipo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoResponseDto {

    private Long id;
    private String descripcionCompleta;
    private String numeroSerie;
    private String color;
    private String observaciones;
    private TipoEquipoDto tipoEquipo;
    private MarcaDto marca;
    private ModeloDto modelo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoEquipoDto {
        private Long id;
        private String descripcion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcaDto {
        private Long id;
        private String descripcion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeloDto {
        private Long id;
        private String descripcion;
    }
}
