# Implementación de Gestión de Empleados

## Resumen de la Implementación

Se ha implementado un sistema completo de gestión de empleados que incluye:

### 1. Creación Automática de Usuario
- Al dar de alta un empleado, se crea automáticamente su usuario
- **Username por defecto**: Documento del empleado (DNI)
- **Password por defecto**: Documento del empleado (DNI)
- Opción de especificar username y password personalizados

### 2. DTOs Creados

#### Empleado DTOs
- **EmpleadoCreateDto**: Para crear empleados con usuario automático
- **EmpleadoListDto**: Para listar empleados (optimizado para tablas)
- **EmpleadoResponseDto**: Respuesta completa con datos del empleado y usuario
- **EmpleadoUpdateDto**: Para actualizar datos del empleado

#### Usuario DTOs
- **PerfilResponseDto**: Información completa del perfil del usuario autenticado
- **CambiarPasswordDto**: Para cambio de contraseña con validaciones

### 3. Endpoints de Empleados (`/api/empleados`)

#### POST `/api/empleados`
- **Rol requerido**: PROPIETARIO
- **Función**: Crea un empleado y automáticamente su usuario
- **Body**:
```json
{
  "tipoEmpleadoId": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "tipoPersonaId": 1,
  "tipoDocumentoId": 1,
  "documento": "12345678",
  "sexo": "M",
  "rolUsuario": "TECNICO",
  "usernamePersonalizado": "jperez",      // Opcional
  "passwordPersonalizada": "password123"   // Opcional
}
```

#### GET `/api/empleados`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Lista empleados con paginación y filtros
- **Parámetros**:
  - `activo` (Boolean): Filtrar por estado (true/false/null para todos)
  - `busqueda` (String): Buscar por nombre, apellido, razón social o documento
  - `page` (Integer): Número de página (default: 0)
  - `size` (Integer): Tamaño de página (default: 10)
  - `sort` (String): Campo de ordenamiento (default: id,DESC)
- **Ejemplo**: `/api/empleados?activo=true&busqueda=juan&page=0&size=10`

#### GET `/api/empleados/{id}`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Obtiene un empleado por ID con toda su información

#### GET `/api/empleados/activos`
- **Rol requerido**: PROPIETARIO o ADMINISTRATIVO
- **Función**: Lista todos los empleados activos sin paginación

#### PUT `/api/empleados/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Actualiza datos de un empleado
- **Body**:
```json
{
  "nombre": "Juan Carlos",
  "apellido": "Pérez García",
  "tipoEmpleadoId": 2,
  "activo": true
}
```

#### PATCH `/api/empleados/{id}/desactivar`
- **Rol requerido**: PROPIETARIO
- **Función**: Baja lógica del empleado y su usuario asociado

#### PATCH `/api/empleados/{id}/activar`
- **Rol requerido**: PROPIETARIO
- **Función**: Reactiva un empleado y su usuario asociado

#### DELETE `/api/empleados/{id}`
- **Rol requerido**: PROPIETARIO
- **Función**: Eliminación física del empleado (hard delete)

### 4. Endpoints de Perfil (`/api/usuarios`)

#### GET `/api/usuarios/mi-perfil`
- **Rol requerido**: Cualquier usuario autenticado
- **Función**: Obtiene el perfil del usuario autenticado
- **Respuesta**:
```json
{
  "usuarioId": 1,
  "username": "jperez",
  "rol": "TECNICO",
  "usuarioActivo": true,
  "fechaCreacion": "2024-01-15T10:30:00",
  "ultimoLogin": "2024-01-20T08:45:00",
  "empleadoId": 1,
  "nombreCompleto": "Juan Pérez",
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "tipoDocumento": "DNI",
  "sexo": "M",
  "tipoEmpleado": "Técnico",
  "empleadoActivo": true
}
```

#### PATCH `/api/usuarios/cambiar-mi-password`
- **Rol requerido**: Cualquier usuario autenticado
- **Función**: Cambiar la contraseña propia
- **Body**:
```json
{
  "passwordActual": "12345678",
  "passwordNueva": "nuevaPassword123",
  "passwordNuevaConfirmacion": "nuevaPassword123"
}
```
- **Validaciones**:
  - La contraseña actual debe ser correcta
  - Las contraseñas nuevas deben coincidir
  - La nueva contraseña debe ser diferente a la actual
  - Mínimo 6 caracteres

### 5. Repositorios Actualizados

#### EmpleadoRepository
- `findByActivoTrue()`: Lista empleados activos
- `findByActivo(Boolean activo, Pageable pageable)`: Paginación por estado
- `buscarEmpleadosConFiltros(Boolean activo, String busqueda, Pageable pageable)`: Búsqueda avanzada
- `findByTipoEmpleado(Long tipoEmpleadoId)`: Filtrar por tipo de empleado
- `existsByDocumento(String documento)`: Verificar existencia de documento

#### PersonaRepository
- `findByDocumento(String documento)`: Buscar persona por documento
- `existsByDocumento(String documento)`: Verificar existencia de documento

### 6. Excepciones
- **EmpleadoNotFoundException**: Empleado no encontrado
- Manejador agregado en GlobalExceptionHandler

### 7. Servicios

#### EmpleadoService
- `crearEmpleado()`: Crea empleado + persona + usuario automáticamente
- `obtenerEmpleadoPorId()`: Obtiene empleado con toda su información
- `obtenerEmpleadosConFiltros()`: Lista con paginación y filtros (backend)
- `obtenerEmpleadosActivos()`: Lista empleados activos
- `actualizarEmpleado()`: Actualiza datos del empleado
- `desactivarEmpleado()`: Baja lógica (empleado + usuario)
- `activarEmpleado()`: Reactiva empleado y usuario
- `eliminarEmpleado()`: Eliminación física

#### UsuarioService (métodos agregados)
- `obtenerPerfil()`: Obtiene perfil del usuario autenticado
- `cambiarPasswordAutenticado()`: Cambio de contraseña con validaciones

## Flujo de Trabajo para el Frontend

### Pantalla de Gestión de Empleados (PROPIETARIO)

#### 1. Listar Empleados con Filtros
```typescript
// Ejemplo con PrimeNG p-table
<p-table 
  [value]="empleados" 
  [lazy]="true" 
  (onLazyLoad)="loadEmpleados($event)"
  [paginator]="true" 
  [rows]="10"
  [totalRecords]="totalRecords"
  [loading]="loading">
  
  <!-- Filtro por estado -->
  <ng-template pTemplate="header">
    <tr>
      <th>
        <p-dropdown 
          [options]="estadoOptions" 
          [(ngModel)]="filtroActivo"
          (onChange)="loadEmpleados($event)">
        </p-dropdown>
      </th>
      <!-- Búsqueda -->
      <th>
        <input 
          pInputText 
          type="text" 
          [(ngModel)]="busqueda"
          (input)="loadEmpleados($event)"
          placeholder="Buscar...">
      </th>
    </tr>
  </ng-template>
</p-table>
```

```typescript
loadEmpleados(event: LazyLoadEvent) {
  this.loading = true;
  const params = {
    page: event.first / event.rows,
    size: event.rows,
    activo: this.filtroActivo, // true/false/null
    busqueda: this.busqueda,
    sort: 'id,DESC'
  };
  
  this.empleadoService.getEmpleados(params).subscribe(response => {
    this.empleados = response.content;
    this.totalRecords = response.totalElements;
    this.loading = false;
  });
}
```

#### 2. Crear Empleado
```typescript
crearEmpleado() {
  const empleado = {
    tipoEmpleadoId: this.form.value.tipoEmpleadoId,
    nombre: this.form.value.nombre,
    apellido: this.form.value.apellido,
    tipoPersonaId: 1, // Física
    tipoDocumentoId: 1, // DNI
    documento: this.form.value.documento,
    sexo: this.form.value.sexo,
    rolUsuario: this.form.value.rolUsuario,
    // Usuario y password serán el documento por defecto
    // O se pueden especificar:
    usernamePersonalizado: this.form.value.username,
    passwordPersonalizada: this.form.value.password
  };
  
  this.empleadoService.crear(empleado).subscribe(
    response => {
      this.messageService.add({
        severity: 'success',
        summary: 'Empleado creado',
        detail: `Usuario: ${response.username} / Password: ${documento}`
      });
    }
  );
}
```

#### 3. Dar de Baja (Lógica)
```typescript
desactivarEmpleado(id: number) {
  this.empleadoService.desactivar(id).subscribe(
    () => {
      this.messageService.add({
        severity: 'success',
        summary: 'Empleado desactivado'
      });
      this.loadEmpleados();
    }
  );
}
```

### Pantalla de Perfil (Todos los empleados)

#### 1. Ver Mi Perfil
```typescript
ngOnInit() {
  this.usuarioService.getMiPerfil().subscribe(
    perfil => {
      this.perfil = perfil;
    }
  );
}
```

#### 2. Cambiar Contraseña
```typescript
cambiarPassword() {
  const data = {
    passwordActual: this.form.value.passwordActual,
    passwordNueva: this.form.value.passwordNueva,
    passwordNuevaConfirmacion: this.form.value.passwordNuevaConfirmacion
  };
  
  this.usuarioService.cambiarMiPassword(data).subscribe(
    () => {
      this.messageService.add({
        severity: 'success',
        summary: 'Contraseña cambiada exitosamente'
      });
      this.form.reset();
    },
    error => {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: error.error.message
      });
    }
  );
}
```

## Consideraciones sobre Paginación y Filtros

**Se implementó en BACKEND** porque:
1. **Performance**: Con muchos empleados, traer todos al frontend es ineficiente
2. **Escalabilidad**: El backend maneja mejor grandes volúmenes de datos
3. **Carga de red**: Solo se transfiere la página actual
4. **Memoria**: El frontend no necesita mantener todos los registros

La paginación se hace con `Pageable` de Spring Data, que ya integra con PrimeNG p-table usando `[lazy]="true"`.

## Seguridad

- **PROPIETARIO**: Puede gestionar empleados (crear, actualizar, activar/desactivar, eliminar)
- **ADMINISTRATIVO**: Puede ver empleados pero no gestionarlos
- **Todos los usuarios autenticados**: Pueden ver y editar su propio perfil y cambiar su contraseña

## Notas Importantes

1. **Credenciales iniciales**: Al crear un empleado, se debe informar al usuario que su username y password inicial es su documento (DNI)
2. **Primer login**: Se recomienda que el empleado cambie su contraseña en el primer inicio de sesión
3. **Baja lógica**: Se usa `activo=false` para mantener el historial. La baja física solo debe usarse en casos excepcionales
4. **Filtros en tabla**: Los filtros se procesan en backend para mejor performance
5. **Usuario único por empleado**: No se puede crear dos usuarios para el mismo empleado

