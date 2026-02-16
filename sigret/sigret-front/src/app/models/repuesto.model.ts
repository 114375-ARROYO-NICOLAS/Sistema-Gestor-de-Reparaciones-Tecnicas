export interface Repuesto {
  id: number;
  descripcion: string;
  tipoEquipo?: string;
  descripcionCompleta: string;
}

export interface RepuestoListDto {
  id: number;
  descripcion: string;
  tipoEquipoId?: number;
  tipoEquipo?: string;
  descripcionCompleta: string;
}

export interface RepuestoCreateDto {
  tipoEquipoId: number;
  descripcion: string;
}

export interface RepuestoUpdateDto {
  tipoEquipoId?: number;
  descripcion?: string;
}

export interface RepuestoResponseDto {
  id: number;
  descripcion: string;
  tipoEquipoId?: number;
  tipoEquipo?: string;
  descripcionCompleta: string;
}
