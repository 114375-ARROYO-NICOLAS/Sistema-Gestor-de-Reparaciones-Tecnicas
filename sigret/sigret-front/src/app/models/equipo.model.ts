// Equipo models for frontend

export interface Equipo {
  id: number;
  descripcionCompleta: string;
  numeroSerie?: string;
  color?: string;
  tipoEquipo: string;
  marca: string;
  modelo?: string;
}

export interface EquipoListDto {
  id: number;
  descripcionCompleta: string;
  numeroSerie?: string;
  color?: string;
  tipoEquipo: string;
  marca: string;
  modelo?: string;
  clienteId?: number;
  clienteNombre?: string;
}

export interface EquipoCreateDto {
  tipoEquipoId: number;
  marcaId: number;
  modeloId?: number;
  numeroSerie?: string;
  color?: string;
  observaciones?: string;
  clienteId?: number; // Para asociar al crear
}

export interface EquipoResponseDto {
  id: number;
  descripcionCompleta: string;
  numeroSerie?: string;
  color?: string;
  observaciones?: string;
  tipoEquipo: {
    id: number;
    descripcion: string;
  };
  marca: {
    id: number;
    descripcion: string;
  };
  modelo?: {
    id: number;
    descripcion: string;
  };
}

export interface EquipoUpdateDto {
  tipoEquipoId?: number;
  marcaId?: number;
  modeloId?: number;
  numeroSerie?: string;
  color?: string;
  observaciones?: string;
}

// For pagination
export interface EquipoListResponse {
  content: EquipoListDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface EquipoFilterParams {
  page?: number;
  size?: number;
  sort?: string;
}
