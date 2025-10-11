# Verificación: Integración Backend-Frontend

## ✅ Estado de los Endpoints

### Endpoints de Catálogo (Ya implementados en backend)

| Endpoint | Frontend | Backend | Estado |
|----------|----------|---------|--------|
| `/api/tipos-empleado` | ✅ Configurado | ✅ Implementado | ✅ Listo |
| `/api/tipos-persona` | ✅ Configurado | ✅ Implementado | ✅ Listo |
| `/api/tipos-documento` | ✅ Configurado | ✅ Implementado | ✅ Listo |
| `/api/empleados` | ✅ Configurado | ✅ Implementado | ✅ Listo |
| `/api/usuarios/mi-perfil` | ✅ Configurado | ✅ Implementado | ✅ Listo |
| `/api/usuarios/cambiar-mi-password` | ✅ Configurado | ✅ Implementado | ✅ Listo |

### Roles de Acceso (Según backend)

| Endpoint | PROPIETARIO | ADMINISTRATIVO | TECNICO |
|----------|-------------|----------------|---------|
| GET `/api/tipos-*` | ✅ Lectura | ✅ Lectura | ❌ Sin acceso |
| POST `/api/tipos-*` | ✅ Crear | ❌ | ❌ |
| PUT `/api/tipos-*` | ✅ Editar | ❌ | ❌ |
| DELETE `/api/tipos-*` | ✅ Eliminar | ❌ | ❌ |
| GET `/api/empleados` | ✅ Ver todos | ✅ Ver todos | ❌ |
| POST `/api/empleados` | ✅ Crear | ❌ | ❌ |

## 🔧 Checklist de Verificación

### 1. Backend debe estar corriendo ✅
```bash
cd sigret-backend
mvn spring-boot:run
```
**Verificar**: `http://localhost:8080/actuator/health` debe responder OK

### 2. Base de datos debe tener datos iniciales ✅

Según `IMPLEMENTACION_CRUD_TIPOS.md`, debe haber un DataLoader que cargue:

**TipoPersona**:
- Física
- Jurídica

**TipoEmpleado**:
- Propietario
- Administrativo
- Técnico

**TipoDocumento**:
- DNI
- CUIT
- CUIL
- Pasaporte

### 3. Usuario debe tener rol adecuado ✅

Para usar el módulo de empleados, necesitas:
- **PROPIETARIO**: Para crear empleados
- **ADMINISTRATIVO**: Solo para ver

### 4. Token válido ✅

El token debe estar activo y no expirado.

## 🧪 Pruebas de Endpoints (Postman/Insomnia)

### Probar Tipos de Empleado
```http
GET http://localhost:8080/api/tipos-empleado
Authorization: Bearer {tu_token}
```

**Respuesta esperada (200 OK)**:
```json
[
  { "id": 1, "descripcion": "Propietario" },
  { "id": 2, "descripcion": "Administrativo" },
  { "id": 3, "descripcion": "Técnico" }
]
```

### Probar Tipos de Persona
```http
GET http://localhost:8080/api/tipos-persona
Authorization: Bearer {tu_token}
```

**Respuesta esperada (200 OK)**:
```json
[
  { "id": 1, "descripcion": "Física" },
  { "id": 2, "descripcion": "Jurídica" }
]
```

### Probar Tipos de Documento
```http
GET http://localhost:8080/api/tipos-documento
Authorization: Bearer {tu_token}
```

**Respuesta esperada (200 OK)**:
```json
[
  { "id": 1, "descripcion": "DNI" },
  { "id": 2, "descripcion": "CUIT" },
  { "id": 3, "descripcion": "CUIL" },
  { "id": 4, "descripcion": "Pasaporte" }
]
```

## 🐛 Solución de Problemas

### Problema 1: Error 401 (Unauthorized)

**Causa**: Token expirado o inválido

**Solución**:
1. Hacer login nuevamente en el frontend
2. Verificar que el token se está guardando en sessionStorage
3. Verificar que el interceptor está agregando el header Authorization

**Verificar en Browser DevTools**:
```javascript
// Console
sessionStorage.getItem('auth_token')  // Debe mostrar el token
```

### Problema 2: Error 403 (Forbidden)

**Causa**: Usuario no tiene el rol adecuado

**Solución**:
1. Verificar el rol del usuario logueado
2. Asegurarse de usar un usuario con rol PROPIETARIO o ADMINISTRATIVO

### Problema 3: Error 404 (Not Found)

**Causa**: El endpoint no existe o la URL es incorrecta

**Verificar**:
- Backend corriendo en puerto 8080
- Environment correcto en frontend (`environment.apiUrl = 'http://localhost:8080/api'`)

### Problema 4: Múltiples toasts "Sesión Expirada"

**Causa**: Ya solucionado con el flag en `session-expiry.interceptor.ts`

**Estado**: ✅ Resuelto

### Problema 5: CORS Error

**Causa**: Backend no permite peticiones desde `http://localhost:4200`

**Solución en Backend**:
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

## ✅ Flujo Completo de Creación de Empleado

### 1. Frontend carga catálogos
```typescript
ngOnInit() {
  this.loadEmployeeTypes();    // GET /api/tipos-empleado
  this.loadPersonTypes();       // GET /api/tipos-persona
  this.loadDocumentTypes();     // GET /api/tipos-documento
}
```

### 2. Usuario completa formulario
```typescript
{
  tipoEmpleadoId: 3,              // Técnico
  tipoPersonaId: 1,               // Física
  tipoDocumentoId: 1,             // DNI
  nombre: "Juan",
  apellido: "Pérez",
  documento: "12345678",
  sexo: "M",
  rolUsuario: "TECNICO",
  usernamePersonalizado: "",      // Opcional
  passwordPersonalizada: ""       // Opcional
}
```

### 3. Frontend envía POST
```http
POST http://localhost:8080/api/empleados
Content-Type: application/json
Authorization: Bearer {token}

{
  "tipoEmpleadoId": 3,
  "tipoPersonaId": 1,
  "tipoDocumentoId": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "sexo": "M",
  "rolUsuario": "TECNICO"
}
```

### 4. Backend responde
```json
{
  "empleadoId": 1,
  "nombreCompleto": "Juan Pérez",
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "tipoDocumento": "DNI",
  "sexo": "M",
  "tipoEmpleado": "Técnico",
  "tipoEmpleadoId": 3,
  "empleadoActivo": true,
  "usuarioId": 1,
  "username": "12345678",        // ← Usuario creado automáticamente
  "rol": "TECNICO",
  "usuarioActivo": true,
  "fechaCreacion": "2025-01-02T10:30:00"
}
```

### 5. Frontend muestra mensaje
```
✅ Empleado creado exitosamente
Usuario: 12345678 | Contraseña: 12345678
(Se muestra por 10 segundos)
```

## 📋 Comandos Útiles

### Ver logs del backend
```bash
cd sigret-backend
tail -f logs/application.log
```

### Ver tabla de tipos en base de datos
```sql
SELECT * FROM tipos_empleado;
SELECT * FROM tipos_persona;
SELECT * FROM tipos_documento;
```

### Limpiar sessionStorage (si hay problemas de token)
```javascript
// En Browser Console
sessionStorage.clear();
localStorage.clear();
```

### Reiniciar backend
```bash
# Ctrl+C para detener
mvn clean spring-boot:run
```

### Reiniciar frontend
```bash
# Ctrl+C para detener
ng serve
```

## 🎯 Pasos para Probar Todo

1. **Iniciar Backend**:
   ```bash
   cd sigret-backend
   mvn spring-boot:run
   ```
   Esperar mensaje: "Started SigretApplication"

2. **Iniciar Frontend**:
   ```bash
   cd sigret-front
   ng serve
   ```
   Esperar mensaje: "Compiled successfully"

3. **Login**:
   - Ir a `http://localhost:4200/login`
   - Usar credenciales de un usuario PROPIETARIO

4. **Navegar a Empleados**:
   - Click en menú "Gestión de Empleados"
   - Si se carga sin errores 401 → ✅ Todo funciona

5. **Crear Empleado**:
   - Click "Nuevo Empleado"
   - Completar formulario (los dropdowns deben tener opciones)
   - Click "Crear"
   - Debe mostrar mensaje con credenciales

## 🎉 Resultado Esperado

### ✅ Si todo está bien:
- No hay toasts de error
- Los dropdowns tienen opciones
- Se puede crear empleados
- Se ve el mensaje con credenciales generadas

### ❌ Si hay problemas:
1. Verificar que backend esté corriendo
2. Verificar que datos iniciales estén cargados
3. Verificar rol del usuario
4. Revisar console del navegador (F12)
5. Revisar logs del backend

## 📝 Notas Finales

- **Frontend**: Ya está 100% configurado ✅
- **Backend**: Ya tiene los endpoints implementados ✅
- **Interceptor**: Ya maneja múltiples toasts correctamente ✅
- **Todo debería funcionar ahora** 🎉

Si aún hay errores 401:
- El problema es de **autenticación** (token expirado)
- **Solución**: Hacer logout y login nuevamente

