export interface Employee {
  id: number;
  tipoEmpleado: EmployeeType;
  persona: Person;
  activo: boolean;
  nombreCompleto: string;
  tieneUsuario?: boolean;
}

export interface EmployeeType {
  id: number;
  descripcion: string;
}

export interface Person {
  id: number;
  tipoPersona: PersonType;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  tipoDocumento: DocumentType;
  documento: string;
  sexo?: string;
}

export interface PersonType {
  id: number;
  descripcion: string;
}

export interface DocumentType {
  id: number;
  descripcion: string;
}

export interface EmployeeCreateRequest {
  tipoEmpleadoId: number;
  persona: PersonCreateRequest;
  activo?: boolean;
}

export interface PersonCreateRequest {
  tipoPersonaId: number;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  tipoDocumentoId: number;
  documento: string;
  sexo?: string;
}

export interface EmployeeUpdateRequest {
  tipoEmpleadoId?: number;
  activo?: boolean;
  persona?: PersonUpdateRequest;
}

export interface PersonUpdateRequest {
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  sexo?: string;
}

export interface EmployeeListResponse {
  content: Employee[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface EmployeeWithoutUser {
  id: number;
  nombreCompleto: string;
  tipoEmpleado: string;
  documento: string;
}
