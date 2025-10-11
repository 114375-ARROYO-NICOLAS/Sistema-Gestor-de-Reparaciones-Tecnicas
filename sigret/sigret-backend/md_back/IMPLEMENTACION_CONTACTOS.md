# Implementación de Gestión de Contactos para Personas

## Fecha: 08/10/2025

## Problema Identificado

Al crear empleados o clientes (que son personas), solo se podían agregar direcciones, pero faltaba la capacidad de agregar contactos (teléfonos, emails, etc.). Los contactos son información esencial para la comunicación con clientes y empleados.

## Solución Implementada

Se implementó un sistema completo de gestión de contactos que permite:
- Crear contactos al crear una persona (cliente o empleado)
- Actualizar contactos al actualizar una persona
- Múltiples contactos por persona (emails, teléfonos, etc.)
- Gestión automática de contactos vinculados a personas

## Archivos Creados

### 1. DTOs de Contacto

#### ContactoCreateDto.java
```java
package com.sigret.dtos.contacto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoCreateDto {

    // PersonaId es opcional cuando se usa desde Empleado/Cliente
    private Long personaId;

    @NotNull(message = "El tipo de contacto es obligatorio")
    private Long tipoContactoId;

    @NotBlank(message = "La descripción del contacto es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
}
```

#### ContactoListDto.java
```java
package com.sigret.dtos.contacto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoListDto {
    
    private Long id;
    private String tipoContacto;
    private String descripcion;
}
```

### 2. Repositorios

#### ContactoRepository.java
```java
package com.sigret.repositories;

import com.sigret.entities.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    
    List<Contacto> findByPersonaId(Long personaId);
    void deleteByPersonaId(Long personaId);
}
```

#### TipoContactoRepository.java
```java
package com.sigret.repositories;

import com.sigret.entities.TipoContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoContactoRepository extends JpaRepository<TipoContacto, Long> {
    
    Optional<TipoContacto> findByDescripcion(String descripcion);
    boolean existsByDescripcion(String descripcion);
}
```

### 3. Excepción

#### TipoContactoNotFoundException.java
```java
package com.sigret.exception;

public class TipoContactoNotFoundException extends RuntimeException {
    public TipoContactoNotFoundException(String message) {
        super(message);
    }
}
```

## Archivos Modificados

### 1. DTOs de Cliente

#### ClienteCreateDto.java
**Agregado:**
```java
import com.sigret.dtos.contacto.ContactoCreateDto;

// ...

// Contactos (opcional, se gestiona automáticamente)
private List<ContactoCreateDto> contactos;

// Direcciones (opcional, se gestiona automáticamente)
private List<DireccionCreateDto> direcciones;
```

#### ClienteUpdateDto.java
**Agregado:**
```java
import com.sigret.dtos.contacto.ContactoCreateDto;

// ...

// Contactos (opcional) - si se envía, reemplaza todos los contactos existentes
private List<ContactoCreateDto> contactos;

// Direcciones (opcional) - si se envía, reemplaza todas las direcciones existentes
private List<DireccionCreateDto> direcciones;
```

### 2. DTOs de Empleado

#### EmpleadoCreateDto.java
**Agregado:**
```java
import com.sigret.dtos.contacto.ContactoCreateDto;

// ...

// Contactos (opcional, se gestiona automáticamente)
private List<ContactoCreateDto> contactos;

// Direcciones (opcional, se gestiona automáticamente)
private List<DireccionCreateDto> direcciones;
```

#### EmpleadoUpdateDto.java
**Agregado:**
```java
import com.sigret.dtos.contacto.ContactoCreateDto;

// ...

// Contactos (opcional) - si se envía, reemplaza todos los contactos existentes
private List<ContactoCreateDto> contactos;

// Direcciones (opcional) - si se envía, reemplaza todas las direcciones existentes
private List<DireccionCreateDto> direcciones;
```

### 3. ClienteServiceImpl.java

**Imports agregados:**
```java
import com.sigret.dtos.contacto.ContactoCreateDto;
import com.sigret.entities.Contacto;
import com.sigret.entities.TipoContacto;
import com.sigret.exception.TipoContactoNotFoundException;
import com.sigret.repositories.ContactoRepository;
import com.sigret.repositories.TipoContactoRepository;
```

**Repositorios inyectados:**
```java
@Autowired
private ContactoRepository contactoRepository;

@Autowired
private TipoContactoRepository tipoContactoRepository;
```

**Métodos agregados:**
```java
/**
 * Crear contactos para una persona
 */
private void crearContactos(Persona persona, List<ContactoCreateDto> contactosDto) {
    for (ContactoCreateDto contactoDto : contactosDto) {
        TipoContacto tipoContacto = tipoContactoRepository.findById(contactoDto.getTipoContactoId())
                .orElseThrow(() -> new TipoContactoNotFoundException("Tipo de contacto no encontrado..."));

        Contacto contacto = new Contacto();
        contacto.setPersona(persona);
        contacto.setTipoContacto(tipoContacto);
        contacto.setDescripcion(contactoDto.getDescripcion());

        contactoRepository.save(contacto);
    }
}

/**
 * Actualizar contactos de una persona (reemplaza todos los existentes)
 */
private void actualizarContactos(Persona persona, List<ContactoCreateDto> contactosDto) {
    List<Contacto> contactosExistentes = contactoRepository.findByPersonaId(persona.getId());
    contactoRepository.deleteAll(contactosExistentes);

    if (contactosDto != null && !contactosDto.isEmpty()) {
        crearContactos(persona, contactosDto);
    }
}
```

**En crearCliente():**
```java
// Crear contactos si fueron proporcionados
if (clienteCreateDto.getContactos() != null && !clienteCreateDto.getContactos().isEmpty()) {
    crearContactos(personaGuardada, clienteCreateDto.getContactos());
}
```

**En actualizarCliente():**
```java
// Actualizar contactos si fueron proporcionados
if (clienteUpdateDto.getContactos() != null) {
    actualizarContactos(persona, clienteUpdateDto.getContactos());
}
```

### 4. EmpleadoService.java

**Imports agregados:**
```java
import com.sigret.dtos.contacto.ContactoCreateDto;
import com.sigret.exception.TipoContactoNotFoundException;
```

**Repositorios inyectados:**
```java
@Autowired
private ContactoRepository contactoRepository;

@Autowired
private TipoContactoRepository tipoContactoRepository;
```

**Métodos agregados:**
```java
/**
 * Crear contactos para una persona
 */
private void crearContactos(Persona persona, List<ContactoCreateDto> contactosDto) {
    for (ContactoCreateDto contactoDto : contactosDto) {
        TipoContacto tipoContacto = tipoContactoRepository.findById(contactoDto.getTipoContactoId())
                .orElseThrow(() -> new TipoContactoNotFoundException("Tipo de contacto no encontrado..."));

        Contacto contacto = new Contacto();
        contacto.setPersona(persona);
        contacto.setTipoContacto(tipoContacto);
        contacto.setDescripcion(contactoDto.getDescripcion());

        contactoRepository.save(contacto);
    }
}

/**
 * Actualizar contactos de una persona (reemplaza todos los existentes)
 */
private void actualizarContactos(Persona persona, List<ContactoCreateDto> contactosDto) {
    List<Contacto> contactosExistentes = contactoRepository.findByPersonaId(persona.getId());
    contactoRepository.deleteAll(contactosExistentes);

    if (contactosDto != null && !contactosDto.isEmpty()) {
        crearContactos(persona, contactosDto);
    }
}
```

**En crearEmpleado():**
```java
// Crear contactos si fueron proporcionados
if (empleadoCreateDto.getContactos() != null && !empleadoCreateDto.getContactos().isEmpty()) {
    crearContactos(persona, empleadoCreateDto.getContactos());
}
```

**En actualizarEmpleado():**
```java
// Actualizar contactos si fueron proporcionados
if (empleadoUpdateDto.getContactos() != null) {
    actualizarContactos(persona, empleadoUpdateDto.getContactos());
}
```

## Tipos de Contacto en Base de Datos

Asegúrate de tener estos tipos de contacto en la tabla `tipos_contacto`:

```sql
-- Ejemplos de tipos de contacto comunes
INSERT INTO tipos_contacto (descripcion) VALUES
('Email'),
('Teléfono'),
('Celular'),
('WhatsApp'),
('Telegram'),
('Fax');
```

## Ejemplo de Uso desde API

### 1. Crear Cliente con Contactos

```json
POST /api/clientes

{
  "tipoPersonaId": 1,
  "tipoDocumentoId": 3,
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "sexo": "M",
  "comentarios": "Cliente VIP",
  "contactos": [
    {
      "tipoContactoId": 1,
      "descripcion": "juan.perez@email.com"
    },
    {
      "tipoContactoId": 3,
      "descripcion": "+54 9 11 1234-5678"
    },
    {
      "tipoContactoId": 4,
      "descripcion": "+54 9 11 1234-5678"
    }
  ],
  "direcciones": [
    {
      "calle": "Av. Siempre Viva",
      "numero": "742",
      "ciudad": "Springfield",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": true
    }
  ]
}
```

### 2. Crear Empleado con Contactos

```json
POST /api/empleados

{
  "tipoEmpleadoId": 1,
  "tipoPersonaId": 1,
  "tipoDocumentoId": 3,
  "nombre": "María",
  "apellido": "González",
  "documento": "87654321",
  "sexo": "F",
  "rolUsuario": "TECNICO",
  "contactos": [
    {
      "tipoContactoId": 1,
      "descripcion": "maria.gonzalez@empresa.com"
    },
    {
      "tipoContactoId": 3,
      "descripcion": "+54 9 11 8765-4321"
    }
  ],
  "direcciones": [
    {
      "calle": "Calle Falsa",
      "numero": "123",
      "ciudad": "Springfield",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": true
    }
  ]
}
```

### 3. Actualizar Cliente - Reemplazar Contactos

```json
PUT /api/clientes/1

{
  "nombre": "Juan Carlos",
  "contactos": [
    {
      "tipoContactoId": 1,
      "descripcion": "nuevo.email@email.com"
    }
  ]
}
```

**Nota:** Al enviar contactos en una actualización, se reemplazan **todos** los contactos existentes con los nuevos. Si no se envía la lista de contactos, se mantienen los existentes.

## Validaciones

### En el DTO
```java
@NotNull(message = "El tipo de contacto es obligatorio")
private Long tipoContactoId;

@NotBlank(message = "La descripción del contacto es obligatoria")
@Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
private String descripcion;
```

### En el Servicio
- ✅ Validación de existencia del tipo de contacto
- ✅ Excepción específica: `TipoContactoNotFoundException`
- ✅ Eliminación en cascada de contactos al actualizar

## Manejo de Errores

### Si tipoContactoId no existe:
```json
{
  "timestamp": "2025-10-08T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Tipo de contacto no encontrado con ID: 99",
  "path": "/api/clientes"
}
```

## Comportamiento

### Al Crear
1. Se crea la persona
2. Se crea el cliente/empleado vinculado a la persona
3. Se crean los contactos vinculados a la persona
4. Se crean las direcciones vinculadas a la persona

### Al Actualizar
- Si se envían contactos: **Reemplaza todos** los contactos existentes
- Si NO se envían contactos: **Mantiene** los contactos existentes
- Igual comportamiento para direcciones

### Ventajas de este Enfoque
✅ Simplicidad: Un solo request para crear/actualizar todo
✅ Consistencia: Misma lógica para clientes y empleados
✅ Flexibilidad: Los contactos son opcionales
✅ Validaciones: Tipos de contacto validados contra la BD

## Integración con Frontend

### Tipos de Contacto

Primero, obtener los tipos de contacto disponibles:
```typescript
// En el servicio
getTiposContacto(): Observable<TipoContacto[]> {
  return this.http.get<TipoContacto[]>(`${this.apiUrl}/tipos-contacto`);
}
```

### Formulario de Cliente/Empleado

```typescript
// En el componente
contactoForm = this.fb.group({
  tipoContactoId: [null, Validators.required],
  descripcion: ['', [Validators.required, Validators.maxLength(200)]]
});

contactos: ContactoForm[] = [];

agregarContacto() {
  this.contactos.push({
    tipoContactoId: this.contactoForm.value.tipoContactoId,
    descripcion: this.contactoForm.value.descripcion
  });
  this.contactoForm.reset();
}

eliminarContacto(index: number) {
  this.contactos.splice(index, 1);
}

crearCliente() {
  const clienteData = {
    ...this.clienteForm.value,
    contactos: this.contactos,
    direcciones: this.direcciones
  };
  
  this.clienteService.crearCliente(clienteData).subscribe(...);
}
```

### Template HTML Sugerido

```html
<div class="contactos-section">
  <h3>Contactos</h3>
  
  <!-- Form para agregar contacto -->
  <div [formGroup]="contactoForm">
    <p-dropdown 
      formControlName="tipoContactoId"
      [options]="tiposContacto"
      optionLabel="descripcion"
      optionValue="id"
      placeholder="Tipo de Contacto">
    </p-dropdown>
    
    <input 
      type="text"
      pInputText
      formControlName="descripcion"
      placeholder="Descripción (email, teléfono, etc.)">
    
    <button 
      pButton
      type="button"
      icon="pi pi-plus"
      label="Agregar"
      (click)="agregarContacto()"
      [disabled]="contactoForm.invalid">
    </button>
  </div>
  
  <!-- Lista de contactos agregados -->
  <p-table [value]="contactos">
    <ng-template pTemplate="header">
      <tr>
        <th>Tipo</th>
        <th>Descripción</th>
        <th>Acciones</th>
      </tr>
    </ng-template>
    <ng-template pTemplate="body" let-contacto let-i="rowIndex">
      <tr>
        <td>{{ getTipoContactoNombre(contacto.tipoContactoId) }}</td>
        <td>{{ contacto.descripcion }}</td>
        <td>
          <button 
            pButton
            type="button"
            icon="pi pi-trash"
            class="p-button-danger"
            (click)="eliminarContacto(i)">
          </button>
        </td>
      </tr>
    </ng-template>
  </p-table>
</div>
```

## Testing Recomendado

### Casos de Prueba
1. ✅ Crear cliente con múltiples contactos
2. ✅ Crear empleado con múltiples contactos
3. ✅ Crear cliente sin contactos (opcional)
4. ✅ Actualizar cliente agregando contactos
5. ✅ Actualizar cliente reemplazando contactos
6. ✅ Actualizar cliente sin modificar contactos
7. ✅ Validar que tipoContactoId debe existir
8. ✅ Validar que descripción no puede estar vacía
9. ✅ Validar longitud máxima de descripción (200 caracteres)

## Resumen

✅ **Creados**: DTOs, Repositorios, Excepción personalizada
✅ **Modificados**: ClienteCreateDto, ClienteUpdateDto, EmpleadoCreateDto, EmpleadoUpdateDto
✅ **Actualizados**: ClienteServiceImpl, EmpleadoService con lógica de contactos
✅ **Validaciones**: Tipo de contacto obligatorio, descripción obligatoria
✅ **Comportamiento**: Contactos opcionales, reemplazo completo en actualización
✅ **Sin errores**: Todos los linters pasan correctamente
✅ **Consistencia**: Mismo patrón para direcciones y contactos

