// Import contact models
import { Contacto, ContactoCreateDto } from './contact.model';

export interface Employee {
  id: number;
  tipoEmpleado: EmployeeType;
  persona: Person;
  activo: boolean;
  nombreCompleto: string;
  tieneUsuario?: boolean;
  contactos?: Contacto[];
  direcciones?: Address[];
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
  direcciones?: Address[];
}

export interface PersonType {
  id: number;
  descripcion: string;
}

export interface DocumentType {
  id: number;
  descripcion: string;
}

// Address interfaces for Google Places integration
export interface Address {
  id?: number;
  placeId?: string;
  calle?: string;
  numero?: string;
  piso?: string;
  departamento?: string;
  ciudad?: string;
  provincia?: string;
  pais?: string;
  codigoPostal?: string;
  esPrincipal?: boolean;
  direccionCompleta?: string;
  direccionFormateada?: string;
  latitud?: number;
  longitud?: number;
  observaciones?: string;
  googlePlacesData?: GooglePlacesData;
}

export interface GooglePlacesData {
  placeId: string;
  formattedAddress: string;
  geometry: {
    location: {
      lat: number;
      lng: number;
    };
  };
  addressComponents: AddressComponent[];
}

export interface AddressComponent {
  longName: string;
  shortName: string;
  types: string[];
}

// DTO para crear empleado con usuario automático
export interface EmployeeCreateRequest {
  tipoEmpleadoId: number;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  tipoPersonaId: number;
  tipoDocumentoId: number;
  documento: string;
  sexo?: string;
  rolUsuario: string; // PROPIETARIO, ADMINISTRATIVO, TECNICO
  usernamePersonalizado?: string; // Opcional, si no se proporciona usa el documento
  passwordPersonalizada?: string; // Opcional, si no se proporciona usa el documento
  contactos?: ContactoCreateDto[]; // Contactos opcionales
  direcciones?: Address[]; // Direcciones opcionales
}

// DTO para respuesta de empleado creado (incluye info de usuario)
export interface EmployeeResponse {
  id?: number;
  empleadoId?: number;
  nombreCompleto: string;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  documento: string;
  tipoDocumento: string;
  tipoPersona?: string;
  sexo?: string;
  tipoEmpleado: string;
  tipoEmpleadoId: number;
  empleadoActivo?: boolean;
  activo?: boolean;
  usuarioId?: number;
  username: string;
  rol?: string;
  rolUsuario?: string;
  usuarioActivo?: boolean;
  fechaCreacion?: string;
  fechaCreacionUsuario?: string;
  ultimoLogin?: string;
  contactos?: Contacto[];
  direcciones?: Address[];
}

// DTO para actualizar empleado
export interface EmployeeUpdateRequest {
  nombre?: string;
  apellido?: string;
  tipoEmpleadoId?: number;
  activo?: boolean;
  contactos?: ContactoCreateDto[]; // Contactos opcionales - reemplaza todos si se envía
  direcciones?: Address[]; // Direcciones opcionales - reemplaza todos si se envía
}

export interface EmployeeListResponse {
  content: EmployeeListDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

// DTO para listar empleados (optimizado para tablas)
export interface EmployeeListDto {
  id: number;
  nombreCompleto: string;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  documento: string;
  tipoDocumento: string;
  tipoPersona?: string;
  sexo?: string;
  tipoEmpleado: string;
  tipoEmpleadoId?: number;
  activo: boolean;
  tieneUsuario: boolean;
  username?: string;
  rolUsuario?: string;
  usuarioActivo?: boolean;
  fechaCreacionUsuario?: string;
  ultimoLogin?: string;
  contactos?: Contacto[];
  direcciones?: Address[];
}

export interface EmployeeWithoutUser {
  id: number;
  nombreCompleto: string;
  tipoEmpleado: string;
  documento: string;
}

// Parámetros de filtro para empleados
export interface EmployeeFilterParams {
  activo?: boolean | null; // true/false/null para todos
  busqueda?: string;
  page?: number;
  size?: number;
  sort?: string;
}
