// Marca models

export interface Marca {
  id: number;
  descripcion: string;
}

export interface MarcaListDto {
  id: number;
  descripcion: string;
}

export interface MarcaCreateDto {
  descripcion: string;
}

export interface MarcaUpdateDto {
  descripcion: string;
}

export interface MarcaResponseDto {
  id: number;
  descripcion: string;
}
