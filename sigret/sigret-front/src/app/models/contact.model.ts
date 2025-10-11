// Contact domain models

// Tipo de contacto (Email, Teléfono, Celular, WhatsApp, etc.)
export interface TipoContacto {
  id: number;
  descripcion: string;
}

// Contacto individual
export interface Contacto {
  id?: number;
  tipoContacto: string;
  tipoContactoId?: number;
  descripcion: string; // El valor del contacto (email, número, etc.)
}

// DTO para crear/actualizar contacto
export interface ContactoCreateDto {
  tipoContactoId: number;
  descripcion: string;
}

// DTO de lista de contacto
export interface ContactoListDto {
  id: number;
  tipoContacto: string;
  descripcion: string;
}

