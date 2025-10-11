// Client domain models
export interface Client {
  id: number;
  persona: Person;
  activo: boolean;
  nombreCompleto: string;
  direcciones?: Address[];
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

// Import contact models
import { Contacto, ContactoCreateDto } from './contact.model';

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

// DTO for creating a client
export interface ClientCreateRequest {
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  tipoPersonaId: number;
  tipoDocumentoId: number;
  documento: string;
  sexo?: string;
  contactos?: ContactoCreateDto[]; // Optional contacts
  direcciones?: Address[]; // Optional addresses
}

// DTO for client response
export interface ClientResponse {
  id: number;
  nombreCompleto: string;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  documento: string;
  tipoDocumento: string;
  tipoPersona: string;
  sexo?: string;
  activo: boolean;
  fechaCreacion?: string;
  contactos?: Contacto[]; // Contact list
  direcciones?: Address[];
}

// DTO for updating a client
export interface ClientUpdateRequest {
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  tipoPersonaId?: number;
  tipoDocumentoId?: number;
  documento?: string;
  sexo?: string;
  contactos?: ContactoCreateDto[]; // Optional contacts - replaces all if sent
  direcciones?: Address[]; // Optional addresses - replaces all if sent
}

// Paginated response for client list
export interface ClientListResponse {
  content: ClientListDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

// DTO for listing clients (optimized for tables)
export interface ClientListDto {
  id: number;
  nombreCompleto: string;
  nombre?: string;
  apellido?: string;
  razonSocial?: string;
  documento: string;
  tipoDocumento?: string;
  tipoPersona?: string;
  sexo?: string;
  activo?: boolean;
  fechaCreacion?: string;
  contactos?: Contacto[];
  direcciones?: Address[];
  // New fields from backend response
  email?: string;
  telefono?: string;
  direccionPrincipal?: string;
  esPersonaJuridica?: boolean;
}

// Filter parameters for clients
export interface ClientFilterParams {
  page?: number;
  size?: number;
  sort?: string;
  filtro?: string; // General search filter
}

// Autocomplete parameters
export interface ClientAutocompleteParams {
  termino: string; // Search term
  limite?: number; // Result limit (default 10)
}

