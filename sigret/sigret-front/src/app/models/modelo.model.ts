// Modelo models

export interface Modelo {
  id: number;
  descripcion: string;
  marcaId: number;
  marca?: string;
}

export interface ModeloListDto {
  id: number;
  descripcion: string;
  marcaId: number;
  marcaDescripcion: string;
}

export interface ModeloCreateDto {
  descripcion: string;
  marcaId: number;
}

export interface ModeloUpdateDto {
  descripcion: string;
  marcaId: number;
}

export interface ModeloResponseDto {
  id: number;
  descripcion: string;
  marca: {
    id: number;
    descripcion: string;
  };
}
