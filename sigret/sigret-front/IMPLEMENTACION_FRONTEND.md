# Implementación Frontend - Gestión de Empleados

## Resumen de Cambios

Se ha actualizado el frontend de Angular para integrarse completamente con la nueva API de gestión de empleados implementada en el backend.

## Archivos Modificados

### 1. Modelos (`src/app/models/`)

#### `employee.model.ts`
- ✅ **EmployeeCreateRequest**: Actualizado para crear empleados con usuario automático
  - Incluye campos: `rolUsuario`, `usernamePersonalizado`, `passwordPersonalizada`
  - Estructura plana (sin objeto `persona` anidado)
- ✅ **EmployeeResponse**: Nuevo DTO con información completa del empleado y usuario
- ✅ **EmployeeListDto**: Optimizado para mostrar empleados en tablas
  - Incluye: `tieneUsuario`, `username`, `rolUsuario`
- ✅ **EmployeeUpdateRequest**: Simplificado según backend
- ✅ **EmployeeFilterParams**: Parámetros de filtrado (activo, búsqueda, paginación)

#### `user.model.ts`
- ✅ **UserProfile**: DTO para perfil completo del usuario autenticado
- ✅ **ChangePasswordRequest**: DTO para cambio de contraseña con validaciones

### 2. Servicios (`src/app/services/`)

#### `employee.service.ts`
- ✅ **getEmployees()**: Actualizado para soportar filtros (activo, búsqueda, paginación)
- ✅ **createEmployee()**: Retorna `EmployeeResponse` con credenciales del usuario creado
- ✅ **getUserRoles()**: Método helper para obtener roles disponibles
- ✅ Ajustados todos los endpoints para coincidir con el backend

#### `user.service.ts`
- ✅ **getMyProfile()**: Obtiene perfil del usuario autenticado (`/api/usuarios/mi-perfil`)
- ✅ **changeMyPassword()**: Cambia contraseña del usuario autenticado

### 3. Componentes

#### `employee-management.component.ts/html/scss` ⭐ ACTUALIZADO COMPLETAMENTE
Características implementadas:

**Filtros:**
- 📊 Filtro por estado (Todos/Activos/Inactivos)
- 🔍 Búsqueda por nombre, apellido o documento
- 🔄 Actualización automática al cambiar filtros

**Tabla de Empleados:**
- 📋 Columnas adicionales: Usuario, Rol
- 🏷️ Tags visuales para estado y rol
- ✅ Indicador si tiene usuario asociado
- 📄 Paginación lazy con backend

**Formulario de Creación:**
- 👤 Campos de empleado (tipo, nombre, apellido, documento, sexo)
- 🔐 Campos de usuario (rol, username opcional, password opcional)
- 💡 Mensaje informativo: "Si no especificas usuario/contraseña, se usa el documento"
- ✨ Validaciones dinámicas según tipo de persona (física/jurídica)

**Formulario de Edición:**
- ✏️ Actualiza nombre, apellido, tipo de empleado, estado
- 🚫 No permite editar datos de documento o usuario (por seguridad)

**Mensajes:**
- ✅ Al crear empleado, muestra las credenciales generadas por 10 segundos
- 🎉 Mensajes de éxito/error para todas las operaciones
- ⚠️ Confirmaciones para activar/desactivar/eliminar

#### `profile.component.ts/html/scss` ⭐ NUEVO
Componente completamente nuevo para gestión de perfil personal:

**Vista de Perfil:**
- 👤 Información personal (nombre, documento, sexo)
- 💼 Información de empleado (tipo, estado)
- 🔑 Información de cuenta (usuario, rol, fecha creación, último login)
- 🎨 Cards organizadas visualmente con PrimeNG

**Cambio de Contraseña:**
- 🔐 Modal con formulario de cambio de contraseña
- ✅ Validaciones:
  - Contraseña actual requerida
  - Contraseña nueva mínimo 6 caracteres
  - Confirmación debe coincidir
  - Nueva contraseña debe ser diferente a la actual (validado en backend)
- 👁️ Toggle para mostrar/ocultar contraseñas
- 💡 Mensajes de ayuda y validación en tiempo real

**Acciones Rápidas:**
- 🔄 Actualizar perfil
- 🔐 Cambiar contraseña
- 🛡️ Tips de seguridad

## Integración con el Backend

### Endpoints Utilizados

#### Empleados (`/api/empleados`)
```
GET    /api/empleados?activo={true|false|null}&busqueda={texto}&page={n}&size={n}
POST   /api/empleados
GET    /api/empleados/{id}
PUT    /api/empleados/{id}
PATCH  /api/empleados/{id}/activar
PATCH  /api/empleados/{id}/desactivar
DELETE /api/empleados/{id}
GET    /api/empleados/activos
```

#### Usuarios (`/api/usuarios`)
```
GET    /api/usuarios/mi-perfil
PATCH  /api/usuarios/cambiar-mi-password
```

### Flujo de Creación de Empleado

1. Usuario completa el formulario:
   - Datos de empleado (obligatorios)
   - Rol de usuario (obligatorio)
   - Username personalizado (opcional, si no se usa documento)
   - Password personalizada (opcional, si no se usa documento)

2. Frontend envía `POST /api/empleados` con todos los datos

3. Backend:
   - Crea la persona
   - Crea el empleado
   - Crea el usuario automáticamente
   - Retorna `EmployeeResponse` con username y toda la info

4. Frontend muestra mensaje:
   ```
   ✅ Empleado creado exitosamente
   Usuario: jperez | Contraseña: 12345678
   (Se muestra por 10 segundos)
   ```

### Flujo de Cambio de Contraseña

1. Usuario navega a "Mi Perfil"
2. Click en "Cambiar Contraseña"
3. Ingresa:
   - Contraseña actual
   - Contraseña nueva
   - Confirmación de contraseña nueva
4. Frontend valida que coincidan
5. Envía `PATCH /api/usuarios/cambiar-mi-password`
6. Backend valida:
   - Contraseña actual correcta
   - Contraseñas nuevas coinciden
   - Nueva diferente de actual
   - Mínimo 6 caracteres
7. Actualiza la contraseña

## Configuración de Rutas

Para usar los nuevos componentes, agrega estas rutas en `app.routes.ts`:

```typescript
{
  path: 'employees',
  component: EmployeeManagementComponent,
  canActivate: [authGuard],
  data: { roles: ['PROPIETARIO', 'ADMINISTRATIVO'] }
},
{
  path: 'profile',
  component: ProfileComponent,
  canActivate: [authGuard]
}
```

## Roles y Permisos

### Gestión de Empleados
- **PROPIETARIO**: Acceso completo (crear, editar, activar, desactivar, eliminar)
- **ADMINISTRATIVO**: Solo visualización (sin botón "Nuevo Empleado")
- **TECNICO**: Sin acceso

### Mi Perfil
- **Todos los roles autenticados**: Pueden ver y editar su propio perfil

## Mejoras Implementadas

### UX/UI
- ✨ Diseño moderno con PrimeNG
- 📊 Cards con estadísticas (Total, Activos, Con Usuario)
- 🎨 Tags coloridos para estados y roles
- 💬 Mensajes informativos y de ayuda
- ⚡ Confirmaciones para acciones destructivas
- 🔍 Búsqueda en tiempo real con debounce

### Performance
- 🚀 Paginación lazy (carga solo lo necesario)
- 📡 Filtrado en backend (no sobrecarga el frontend)
- ⚡ Signals de Angular para reactividad óptima
- 🎯 ChangeDetection OnPush (mejora rendimiento)

### Seguridad
- 🔐 Credenciales mostradas solo por 10 segundos
- 🛡️ Validaciones de contraseña robustas
- ✅ No se puede editar documento ni usuario de empleado existente
- 🚫 Confirmaciones para acciones críticas

## Buenas Prácticas Aplicadas

### Angular
- ✅ Standalone components (sin NgModules)
- ✅ Signals para state management
- ✅ `inject()` en lugar de constructor injection
- ✅ Control flow syntax (`@if`, `@for`)
- ✅ Reactive Forms con validaciones
- ✅ OnPush change detection
- ✅ Tipos estrictos de TypeScript

### PrimeNG
- ✅ Componentes optimizados (p-table lazy, p-select, p-dialog)
- ✅ PrimeFlex para layout responsive
- ✅ Iconos con PrimeIcons
- ✅ Toast notifications
- ✅ Confirmation dialogs

## Testing Recomendado

Para probar la implementación:

1. **Crear Empleado:**
   - Crear sin username/password personalizados → debe usar documento
   - Crear con username/password personalizados → debe usar los especificados
   - Verificar que aparece mensaje con credenciales

2. **Filtros:**
   - Filtrar por "Activos" → solo muestra activos
   - Buscar por nombre → encuentra coincidencias
   - Cambiar página → carga nueva página del backend

3. **Editar Empleado:**
   - Cambiar nombre → se actualiza
   - Activar/Desactivar → cambia estado del empleado Y usuario

4. **Mi Perfil:**
   - Verificar que muestra toda la información
   - Cambiar contraseña con contraseña incorrecta → error
   - Cambiar contraseña correctamente → éxito
   - Intentar usar la misma contraseña → error

## Notas Importantes

1. **Credenciales iniciales**: Siempre informar al empleado sus credenciales al crearlo
2. **Primer login**: Recomendar cambiar contraseña en el primer acceso
3. **Baja lógica vs física**: Usar desactivar (baja lógica) por defecto
4. **Filtros**: Los filtros se procesan en backend para mejor performance
5. **Usuario único**: No se puede crear dos usuarios para el mismo empleado

## Próximos Pasos (Opcionales)

- [ ] Agregar exportación de empleados a Excel/PDF
- [ ] Implementar búsqueda avanzada con más filtros
- [ ] Agregar historial de cambios en empleados
- [ ] Implementar foto de perfil
- [ ] Notificación cuando se crea un empleado (email con credenciales)
- [ ] Forzar cambio de contraseña en primer login
- [ ] Política de contraseñas más estricta

## Conclusión

El frontend ahora está completamente integrado con el backend de gestión de empleados, siguiendo las mejores prácticas de Angular 18+ y proporcionando una experiencia de usuario moderna y eficiente. Todos los endpoints documentados en `IMPLEMENTACION_GESTION_EMPLEADOS.md` están implementados y funcionando.

