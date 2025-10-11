# Configuración de Endpoints Públicos en Backend

## Fecha: 08/10/2025

## 🐛 Problema

El endpoint `/api/tipos-contacto` está protegido por autenticación y devuelve 401 cuando el token expira o no existe, causando que el usuario sea deslogueado.

## ⚠️ Error en Consola
```
GET http://localhost:8080/api/tipos-contacto 401 (Unauthorized)
Error: Token de acceso requerido
```

## ✅ Solución

Los endpoints de **catálogos** (tipos de persona, documento, contacto, etc.) deben ser **públicos** porque:
1. Son datos estáticos que no contienen información sensible
2. Se necesitan antes de crear/editar (no siempre hay sesión activa)
3. No cambian frecuentemente
4. Mejoran la UX al cargar más rápido

## 📝 Prompt para Backend

```
Necesito hacer públicos los endpoints de catálogos en SecurityConfig.

PROBLEMA:
El endpoint /api/tipos-contacto devuelve 401 y causa que los usuarios sean deslogueados cuando intentan cargar tipos de contacto.

SOLUCIÓN:
En tu archivo SecurityConfig.java (o similar), busca la sección donde defines requestMatchers públicos y agrega:

.requestMatchers("/api/tipos-contacto").permitAll()

EJEMPLO COMPLETO:

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // Endpoints públicos
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/tipos-persona").permitAll()
            .requestMatchers("/api/tipos-documento").permitAll()
            .requestMatchers("/api/tipos-empleado").permitAll()
            .requestMatchers("/api/tipos-contacto").permitAll()  // ← AGREGAR ESTA LÍNEA
            
            // Swagger (si está habilitado)
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            
            // Todos los demás requieren autenticación
            .anyRequest().authenticated()
        )
        // ... resto de la configuración
    return http.build();
}

IMPORTANTE: 
Todos los endpoints de tipos/catálogos deberían ser públicos porque son datos de referencia que se usan en formularios.
```

## 📋 Endpoints que Deberían Ser Públicos

### Catálogos Esenciales
```
✅ /api/tipos-persona       - Tipos de persona (Física/Jurídica)
✅ /api/tipos-documento     - Tipos de documento (DNI, CUIL, etc.)
✅ /api/tipos-empleado      - Tipos de empleado
🔧 /api/tipos-contacto      - Tipos de contacto (Email, Teléfono, etc.) ← FALTA ESTE
```

### Autenticación
```
✅ /api/auth/login          - Login público
✅ /api/auth/refresh        - Refresh token público
✅ /api/auth/logout         - Logout (puede requerir auth)
```

### Documentación (Desarrollo)
```
✅ /v3/api-docs/**         - OpenAPI spec
✅ /swagger-ui/**          - Swagger UI
✅ /swagger-ui.html        - Swagger UI
```

## 🔒 Endpoints que SÍ Deben Estar Protegidos

```
🔒 /api/clientes/**        - Gestión de clientes
🔒 /api/empleados/**       - Gestión de empleados
🔒 /api/usuarios/**        - Gestión de usuarios
🔒 /api/reparaciones/**    - Gestión de reparaciones (futuro)
```

## 🧪 Cómo Verificar

### Antes de la Corrección
```bash
# Sin token, debe fallar
curl http://localhost:8080/api/tipos-contacto
# Response: 401 Unauthorized
```

### Después de la Corrección
```bash
# Sin token, debe funcionar
curl http://localhost:8080/api/tipos-contacto
# Response: 200 OK
# Body: [{"id":1,"descripcion":"Email"}, ...]
```

## 🔄 Beneficios de Hacer Públicos los Catálogos

### 1. Mejor Experiencia de Usuario
- ✅ Formularios se cargan más rápido
- ✅ No hay delays esperando autenticación
- ✅ No se cierra sesión por cargar catálogos

### 2. Mejor Performance
- ✅ Menos validaciones de token
- ✅ Posibilidad de cachear en CDN
- ✅ Menos carga en el servidor de auth

### 3. Mejor Arquitectura
- ✅ Separación de datos públicos vs privados
- ✅ Más escalable
- ✅ Fácil de cachear

### 4. Prevención de Errores
- ✅ No más 401 en catálogos
- ✅ No más cierre de sesión involuntario
- ✅ Menos errores en consola

## 📚 Referencia

### Patrones Comunes en Seguridad Spring

```java
// Datos públicos
.requestMatchers("/api/public/**").permitAll()
.requestMatchers("/api/tipos-*").permitAll()  // Wildcard para todos los tipos
.requestMatchers("/api/catalogos/**").permitAll()

// Autenticación
.requestMatchers("/api/auth/**").permitAll()

// Documentación
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

// Todo lo demás requiere auth
.anyRequest().authenticated()
```

## ⚡ Solución Rápida Alternativa

Si no puedes modificar el backend ahora, el frontend ya está preparado para manejar el error silenciosamente:

```typescript
// En client-detail y employee-detail
private loadTiposContacto(): void {
  this.clientService.getTiposContacto().subscribe({
    next: (tipos) => {
      this.tiposContacto.set(tipos);
    },
    error: (error) => {
      console.error('Error loading tipos contacto:', error);
      // ✅ No muestra error al usuario
      // ✅ Set empty array para prevenir crashes
      this.tiposContacto.set([]);
    }
  });
}
```

**Con esta solución temporal:**
- No se cierra la sesión
- No se muestra error al usuario
- El select de tipos de contacto estará vacío
- Puedes seguir usando el sistema

## 🔧 Recomendación

**Hacer ambas cosas:**
1. ✅ **Backend:** Hacer público el endpoint (solución definitiva)
2. ✅ **Frontend:** Manejar error silenciosamente (ya implementado)

De esta forma:
- Si el backend está bien configurado → funciona perfecto
- Si hay algún problema temporal → no rompe la aplicación

## 🎯 Acción Inmediata

1. **Abre el cursor del backend**
2. **Busca:** `SecurityConfig.java` o el archivo donde configuras security
3. **Agrega:** `.requestMatchers("/api/tipos-contacto").permitAll()`
4. **Reinicia** el backend
5. **Prueba** nuevamente

Ahora el frontend ya no te deslogueará si falla, pero lo ideal es hacer público ese endpoint.

