// TipoEquipo models

export interface TipoEquipo {
  id: number;
  descripcion: string;
}

export interface TipoEquipoListDto {
  id: number;
  descripcion: string;
}

export interface TipoEquipoCreateDto {
  descripcion: string;
}

export interface TipoEquipoUpdateDto {
  descripcion: string;
}

export interface TipoEquipoResponseDto {
  id: number;
  descripcion: string;
}
