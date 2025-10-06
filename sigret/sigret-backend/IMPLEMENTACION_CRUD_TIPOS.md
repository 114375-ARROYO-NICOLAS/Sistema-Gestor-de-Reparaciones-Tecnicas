# Implementación CRUD - Tipos (Persona, Empleado, Documento)

## Resumen de la Implementación

Se ha implementado el **CRUD completo** para las 3 entidades maestras del sistema:
- **TipoPersona** (Física, Jurídica)
- **TipoEmpleado** (Técnico, Administrativo, etc.)
- **TipoDocumento** (DNI, CUIT, Pasaporte, etc.)

### Archivos Creados

#### DTOs para TipoPersona (4 archivos)
- `TipoPersonaCreateDto.java` - Crear tipo de persona
- `TipoPersonaListDto.java` - Listar tipos de persona
- `TipoPersonaResponseDto.java` - Respuesta completa
- `TipoPersonaUpdateDto.java` - Actualizar tipo de persona

#### DTOs para TipoEmpleado (4 archivos)
- `TipoEmpleadoCreateDto.java` - Crear tipo de empleado
- `TipoEmpleadoListDto.java` - Listar tipos de empleado
- `TipoEmpleadoResponseDto.java` - Respuesta completa
- `TipoEmpleadoUpdateDto.java` - Actualizar tipo de empleado

#### DTOs para TipoDocumento (4 archivos)
- `TipoDocumentoCreateDto.java` - Crear tipo de documento
- `TipoDocumentoListDto.java` - Listar tipos de documento
- `TipoDocumentoResponseDto.java` - Respuesta completa
- `TipoDocumentoUpdateDto.java` - Actualizar tipo de documento

#### Excepciones (4 archivos)
- `TipoPersonaNotFoundException.java` - Tipo de persona no encontrado
- `TipoEmpleadoNotFoundException.java` - Tipo de empleado no encontrado
- `TipoDocumentoNotFoundException.java` - Tipo de documento no encontrado
- `TipoAlreadyExistsException.java` - Para evitar duplicados por descripción

#### Servicios (3 archivos)
- `TipoPersonaService.java` - Lógica de negocio
- `TipoEmpleadoService.java` - Lógica de negocio
- `TipoDocumentoService.java` - Lógica de negocio

#### Controladores (3 archivos)
- `TipoPersonaController.java` - 5 endpoints REST
- `TipoEmpleadoController.java` - 5 endpoints REST
- `TipoDocumentoController.java` - 5 endpoints REST

#### Repositorios Actualizados (3 archivos)
- `TipoPersonaRepository.java` - Queries personalizadas
- `TipoEmpleadoRepository.java` - Queries personalizadas
- `TipoDocumentoRepository.java` - Queries personalizadas

#### GlobalExceptionHandler
- Se agregaron manejadores para las 4 nuevas excepciones

---

## Endpoints Implementados

### 1. Tipos de Persona (`/api/tipos-persona`)

#### POST `/api/tipos-persona`
- **Rol requerido**: PROPIETARIO
- **Función**: Crear un nuevo tipo de persona
- **Body**:
```json
{
  "descripcion": "Física"
}
```
- **Respuesta 201**:
```json
{
  "id": 1,
  "descripcion": "Física"
}
```

#### GET `/api/tipos-persona`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Listar todos los tipos de persona
- **Respuesta 200**:
```json
[
  {
    "id": 1,
    "descripcion": "Física"
  },
  {
    "id": 2,
    "descripcion": "Jurídica"
  }
]
```

#### GET `/api/tipos-persona/{id}`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Obtener un tipo de persona por ID
- **Respuesta 200**:
```json
{
  "id": 1,
  "descripcion": "Física"
}
```

#### PUT `/api/tipos-persona/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Actualizar un tipo de persona
- **Body**:
```json
{
  "descripcion": "Persona Física"
}
```

#### DELETE `/api/tipos-persona/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Eliminar un tipo de persona

---

### 2. Tipos de Empleado (`/api/tipos-empleado`)

#### POST `/api/tipos-empleado`
- **Rol requerido**: PROPIETARIO
- **Función**: Crear un nuevo tipo de empleado
- **Body**:
```json
{
  "descripcion": "Técnico"
}
```
- **Respuesta 201**:
```json
{
  "id": 1,
  "descripcion": "Técnico"
}
```

#### GET `/api/tipos-empleado`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Listar todos los tipos de empleado
- **Respuesta 200**:
```json
[
  {
    "id": 1,
    "descripcion": "Técnico"
  },
  {
    "id": 2,
    "descripcion": "Administrativo"
  },
  {
    "id": 3,
    "descripcion": "Propietario"
  }
]
```

#### GET `/api/tipos-empleado/{id}`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Obtener un tipo de empleado por ID

#### PUT `/api/tipos-empleado/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Actualizar un tipo de empleado

#### DELETE `/api/tipos-empleado/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Eliminar un tipo de empleado

---

### 3. Tipos de Documento (`/api/tipos-documento`)

#### POST `/api/tipos-documento`
- **Rol requerido**: PROPIETARIO
- **Función**: Crear un nuevo tipo de documento
- **Body**:
```json
{
  "descripcion": "DNI"
}
```
- **Respuesta 201**:
```json
{
  "id": 1,
  "descripcion": "DNI"
}
```

#### GET `/api/tipos-documento`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Listar todos los tipos de documento
- **Respuesta 200**:
```json
[
  {
    "id": 1,
    "descripcion": "DNI"
  },
  {
    "id": 2,
    "descripcion": "CUIT"
  },
  {
    "id": 3,
    "descripcion": "Pasaporte"
  }
]
```

#### GET `/api/tipos-documento/{id}`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Obtener un tipo de documento por ID

#### PUT `/api/tipos-documento/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Actualizar un tipo de documento

#### DELETE `/api/tipos-documento/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Eliminar un tipo de documento

---

## Validaciones Implementadas

### 1. Validación de Duplicados
- **No se pueden crear tipos con descripciones duplicadas** (case-insensitive)
- Al intentar crear o actualizar, se verifica que no exista otro tipo con la misma descripción
- Si existe, se lanza `TipoAlreadyExistsException` (HTTP 409 Conflict)

### 2. Validación de Existencia
- Todas las operaciones de lectura, actualización y eliminación verifican que el tipo exista
- Si no existe, se lanza la excepción correspondiente (HTTP 404 Not Found)

### 3. Validación de Entrada
- La descripción es obligatoria (`@NotBlank`)
- Límites de caracteres:
  - TipoPersona: 50 caracteres
  - TipoEmpleado: 50 caracteres
  - TipoDocumento: 30 caracteres

---

## Ejemplos de Uso en Frontend

### 1. Cargar Tipos de Persona (para un dropdown)
```typescript
// Service
getTiposPersona(): Observable<TipoPersonaListDto[]> {
  return this.http.get<TipoPersonaListDto[]>('/api/tipos-persona');
}

// Component
ngOnInit() {
  this.tipoPersonaService.getTiposPersona().subscribe(
    tipos => {
      this.tiposPersona = tipos;
    }
  );
}

// Template con PrimeNG
<p-dropdown 
  [options]="tiposPersona" 
  [(ngModel)]="selectedTipoPersona"
  optionLabel="descripcion" 
  optionValue="id"
  placeholder="Seleccione tipo de persona">
</p-dropdown>
```

### 2. Crear un Nuevo Tipo (Modal de administración)
```typescript
crearTipoEmpleado() {
  const nuevoTipo = {
    descripcion: this.form.value.descripcion
  };
  
  this.tipoEmpleadoService.crear(nuevoTipo).subscribe(
    response => {
      this.messageService.add({
        severity: 'success',
        summary: 'Tipo de empleado creado exitosamente'
      });
      this.cargarTipos();
      this.displayDialog = false;
    },
    error => {
      if (error.status === 409) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Ya existe un tipo de empleado con esa descripción'
        });
      }
    }
  );
}
```

### 3. Actualizar un Tipo
```typescript
actualizarTipoDocumento(id: number) {
  const tipoActualizado = {
    descripcion: this.form.value.descripcion
  };
  
  this.tipoDocumentoService.actualizar(id, tipoActualizado).subscribe(
    response => {
      this.messageService.add({
        severity: 'success',
        summary: 'Tipo de documento actualizado exitosamente'
      });
      this.cargarTipos();
    }
  );
}
```

### 4. Eliminar un Tipo
```typescript
eliminarTipoPersona(id: number) {
  this.confirmationService.confirm({
    message: '¿Está seguro de eliminar este tipo de persona?',
    accept: () => {
      this.tipoPersonaService.eliminar(id).subscribe(
        () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Tipo de persona eliminado exitosamente'
          });
          this.cargarTipos();
        },
        error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error al eliminar',
            detail: 'No se puede eliminar porque está en uso'
          });
        }
      );
    }
  });
}
```

---

## Pantalla de Administración de Tipos

### Estructura Sugerida

```
📁 admin/
  📁 tipos/
    📄 tipos-persona.component.ts/html/scss
    📄 tipos-empleado.component.ts/html/scss
    📄 tipos-documento.component.ts/html/scss
```

### Ejemplo de Template (con PrimeNG p-table)

```html
<p-table [value]="tipos" [loading]="loading">
  <ng-template pTemplate="caption">
    <div class="flex justify-content-between">
      <h2>Tipos de Persona</h2>
      <button 
        pButton 
        label="Nuevo" 
        icon="pi pi-plus" 
        (click)="mostrarDialogCrear()">
      </button>
    </div>
  </ng-template>
  
  <ng-template pTemplate="header">
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Acciones</th>
    </tr>
  </ng-template>
  
  <ng-template pTemplate="body" let-tipo>
    <tr>
      <td>{{ tipo.id }}</td>
      <td>{{ tipo.descripcion }}</td>
      <td>
        <button 
          pButton 
          icon="pi pi-pencil" 
          class="p-button-rounded p-button-text"
          (click)="editar(tipo)">
        </button>
        <button 
          pButton 
          icon="pi pi-trash" 
          class="p-button-rounded p-button-text p-button-danger"
          (click)="eliminar(tipo.id)">
        </button>
      </td>
    </tr>
  </ng-template>
</p-table>

<!-- Dialog para crear/editar -->
<p-dialog 
  [(visible)]="displayDialog" 
  [header]="esEdicion ? 'Editar Tipo' : 'Nuevo Tipo'"
  [modal]="true">
  <form [formGroup]="form">
    <div class="field">
      <label>Descripción</label>
      <input 
        pInputText 
        formControlName="descripcion" 
        placeholder="Ingrese descripción">
    </div>
  </form>
  
  <ng-template pTemplate="footer">
    <button 
      pButton 
      label="Cancelar" 
      (click)="displayDialog = false" 
      class="p-button-text">
    </button>
    <button 
      pButton 
      label="Guardar" 
      (click)="guardar()" 
      [disabled]="!form.valid">
    </button>
  </ng-template>
</p-dialog>
```

---

## Consideraciones Importantes

### 1. Datos Iniciales
Estos catálogos suelen cargarse al inicializar la aplicación. Se recomienda:
- Crear un `DataLoader` o usar el existente para cargar tipos por defecto
- Ejemplos:
  - **TipoPersona**: Física, Jurídica
  - **TipoEmpleado**: Propietario, Administrativo, Técnico
  - **TipoDocumento**: DNI, CUIT, CUIL, Pasaporte

### 2. Eliminación
- La eliminación es **hard delete** (física)
- Si un tipo está en uso (referenciado por empleados/personas), la eliminación **fallará**
- Considerar implementar baja lógica si se requiere mantener histórico

### 3. Seguridad
- **PROPIETARIO**: Full control (crear, editar, eliminar)
- **ADMINISTRATIVO**: Solo lectura
- **TECNICO**: Sin acceso

### 4. Cache en Frontend
Estos catálogos cambian raramente, se puede implementar cache:
```typescript
private tiposPersonaCache$: Observable<TipoPersonaListDto[]>;

getTiposPersona(): Observable<TipoPersonaListDto[]> {
  if (!this.tiposPersonaCache$) {
    this.tiposPersonaCache$ = this.http.get<TipoPersonaListDto[]>('/api/tipos-persona')
      .pipe(shareReplay(1));
  }
  return this.tiposPersonaCache$;
}

limpiarCache() {
  this.tiposPersonaCache$ = null;
}
```

---

## Resumen de Códigos de Estado HTTP

| Código | Significado | Cuándo se usa |
|--------|-------------|---------------|
| 200 | OK | GET, PUT, DELETE exitosos |
| 201 | Created | POST exitoso |
| 400 | Bad Request | Validación de datos fallida |
| 404 | Not Found | Tipo no encontrado |
| 409 | Conflict | Tipo duplicado |
| 500 | Internal Error | Error inesperado |

---

## Integración con Empleados

Estos tipos se usan al crear empleados:

```typescript
// Al crear un empleado
const empleado = {
  tipoEmpleadoId: 1,        // ← Usa TipoEmpleado
  tipoPersonaId: 1,         // ← Usa TipoPersona
  tipoDocumentoId: 1,       // ← Usa TipoDocumento
  nombre: "Juan",
  apellido: "Pérez",
  documento: "12345678",
  // ...
};
```

Por eso es importante que estos catálogos estén cargados antes de mostrar el formulario de empleados.

