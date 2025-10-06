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

// DTO para el perfil del usuario autenticado
export interface UserProfile {
  usuarioId: number;
  username: string;
  rol: UserRole;
  usuarioActivo: boolean;
  fechaCreacion: string;
  ultimoLogin?: string;
  empleadoId: number;
  nombreCompleto: string;
  nombre?: string;
  apellido?: string;
  documento: string;
  tipoDocumento: string;
  sexo?: string;
  tipoEmpleado: string;
  empleadoActivo: boolean;
}

// DTO para cambiar la contraseña del usuario autenticado
export interface ChangePasswordRequest {
  passwordActual: string;
  passwordNueva: string;
  passwordNuevaConfirmacion: string;
}