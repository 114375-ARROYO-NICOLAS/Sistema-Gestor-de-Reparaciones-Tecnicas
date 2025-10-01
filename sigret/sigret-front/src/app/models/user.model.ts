export enum UserRole {
  PROPIETARIO = 'PROPIETARIO',
  ADMINISTRATIVO = 'ADMINISTRATIVO',
  TECNICO = 'TECNICO'
}

export interface User {
  id: number;
  username: string;
  nombreCompleto: string;
  empleadoId: number;
  empleadoNombre: string;
  rol: UserRole;
  activo: boolean;
  fechaCreacion: string;
  ultimoLogin?: string;
}

export interface UserCreateRequest {
  empleadoId: number;
  username: string;
  password: string;
  rol: UserRole;
  activo?: boolean;
}

export interface UserUpdateRequest {
  username?: string;
  password?: string;
  rol?: UserRole;
  activo?: boolean;
}

export interface UserListResponse {
  content: User[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface UsernameAvailabilityResponse {
  disponible: boolean;
}
