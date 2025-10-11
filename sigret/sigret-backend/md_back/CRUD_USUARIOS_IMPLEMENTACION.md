# Implementación CRUD para Gestión de Usuarios

## Resumen
Se ha implementado un sistema completo de CRUD para la gestión de usuarios del sistema SIGRET, permitiendo al rol PROPIETARIO gestionar todos los usuarios del sistema.

## Estructura Implementada

### 1. DTOs (Data Transfer Objects)
- **UsuarioCreateDto**: Para crear nuevos usuarios
- **UsuarioUpdateDto**: Para actualizar usuarios existentes
- **UsuarioResponseDto**: Para respuestas detalladas
- **UsuarioListDto**: Para listados de usuarios

### 2. Servicios
- **UsuarioService**: Servicio principal con todas las operaciones CRUD
  - `crearUsuario()`: Crear nuevo usuario
  - `obtenerUsuarioPorId()`: Obtener usuario por ID
  - `obtenerUsuarios()`: Listar usuarios con paginación
  - `obtenerUsuariosActivos()`: Listar usuarios activos
  - `buscarUsuariosPorUsername()`: Buscar usuarios por username
  - `actualizarUsuario()`: Actualizar usuario
  - `desactivarUsuario()`: Desactivar usuario (soft delete)
  - `activarUsuario()`: Activar usuario
  - `eliminarUsuario()`: Eliminar usuario (hard delete)
  - `cambiarPassword()`: Cambiar contraseña
  - `isUsernameDisponible()`: Verificar disponibilidad de username

### 3. Controlador REST
- **UsuarioController**: Endpoints REST para gestión de usuarios
  - `POST /api/usuarios`: Crear usuario
  - `GET /api/usuarios/{id}`: Obtener usuario por ID
  - `GET /api/usuarios`: Listar usuarios (paginado)
  - `GET /api/usuarios/activos`: Listar usuarios activos
  - `GET /api/usuarios/buscar`: Buscar usuarios por username
  - `GET /api/usuarios/username/{username}`: Obtener usuario por username
  - `PUT /api/usuarios/{id}`: Actualizar usuario
  - `PATCH /api/usuarios/{id}/desactivar`: Desactivar usuario
  - `PATCH /api/usuarios/{id}/activar`: Activar usuario
  - `DELETE /api/usuarios/{id}`: Eliminar usuario
  - `PATCH /api/usuarios/{id}/cambiar-password`: Cambiar contraseña
  - `GET /api/usuarios/verificar-username`: Verificar disponibilidad de username

### 4. Manejo de Excepciones
- **UsuarioNotFoundException**: Usuario no encontrado
- **UsernameAlreadyExistsException**: Username ya existe
- **EmpleadoAlreadyHasUserException**: Empleado ya tiene usuario
- **GlobalExceptionHandler**: Manejo centralizado de excepciones

### 5. Seguridad
- Control de acceso basado en roles
- Solo PROPIETARIO puede crear, actualizar y eliminar usuarios
- ADMINISTRATIVO puede consultar usuarios
- Validaciones de seguridad en todos los endpoints

## Características Implementadas

### Validaciones
- Username único en el sistema
- Un empleado solo puede tener un usuario
- Validación de contraseñas (6-50 caracteres)
- Validación de roles de usuario

### Seguridad
- Contraseñas encriptadas con BCrypt
- Control de acceso por roles
- Soft delete para desactivar usuarios
- Validación de permisos en cada operación

### Funcionalidades
- Paginación en listados
- Búsqueda por username
- Filtrado de usuarios activos
- Cambio de contraseñas
- Activación/desactivación de usuarios
- Verificación de disponibilidad de username

## Endpoints Disponibles

### Crear Usuario
```http
POST /api/usuarios
Authorization: Bearer {token}
Content-Type: application/json

{
  "empleadoId": 1,
  "username": "usuario1",
  "password": "password123",
  "rol": "TECNICO",
  "activo": true
}
```

### Obtener Usuario
```http
GET /api/usuarios/1
Authorization: Bearer {token}
```

### Listar Usuarios
```http
GET /api/usuarios?page=0&size=10&sort=username,asc
Authorization: Bearer {token}
```

### Actualizar Usuario
```http
PUT /api/usuarios/1
Authorization: Bearer {token}
Content-Type: application/json

{
  "password": "nuevapassword",
  "rol": "ADMINISTRATIVO",
  "activo": true
}
```

### Desactivar Usuario
```http
PATCH /api/usuarios/1/desactivar
Authorization: Bearer {token}
```

## Roles y Permisos

- **PROPIETARIO**: Acceso completo a todas las operaciones CRUD
- **ADMINISTRATIVO**: Solo consulta de usuarios
- **TECNICO**: Sin acceso a gestión de usuarios

## Consideraciones de Seguridad

1. Solo usuarios con rol PROPIETARIO pueden gestionar usuarios
2. Las contraseñas se encriptan automáticamente
3. Se valida que un empleado no tenga múltiples usuarios
4. Se valida que el username sea único
5. Soft delete para mantener integridad referencial
6. Validación de permisos en cada endpoint
