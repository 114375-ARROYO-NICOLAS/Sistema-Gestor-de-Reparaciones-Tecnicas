# Solución: Múltiples Toasts de Sesión Expirada

## Problema Original

Al navegar a cualquier ruta que no sea el dashboard, aparecían múltiples toasts de error "Sesión Expirada" y el usuario era redirigido automáticamente al dashboard.

## Causa Raíz

El problema tenía **tres causas principales**:

### 1. Duplicación de Mensajes Toast
- El `session-expiry.interceptor` mostraba un toast cuando detectaba un error 401
- El `AuthService.showSessionExpiredAlert()` también mostraba un toast
- **Resultado**: 2 toasts por cada error de sesión

### 2. Múltiples Peticiones HTTP Fallando
Cuando se cargaba un componente como `employee-management`, se hacían múltiples peticiones:
- `GET /api/empleados` (lista de empleados)
- `GET /api/tipos-empleado` (catálogo)
- `GET /api/tipos-persona` (catálogo)
- `GET /api/tipos-documento` (catálogo)

Si el token estaba expirado, **cada una de estas peticiones** generaba un error 401, y cada error mostraba 2 toasts (interceptor + AuthService).

**Resultado**: 8 toasts para un solo componente (4 peticiones × 2 toasts por petición)

### 3. Validación Automática al Inicializar
El `AuthService` en su constructor llamaba a `checkTokenValidity()`, que validaba el token automáticamente. Si el token estaba expirado, esta validación también generaba errores 401 adicionales.

## Soluciones Implementadas

### ✅ 1. Flag para Evitar Toasts Duplicados en el Interceptor

```typescript
// session-expiry.interceptor.ts
let sessionExpiredShown = false;

export const sessionExpiryInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/')) {
        // Solo mostrar el mensaje una vez
        if (!sessionExpiredShown) {
          sessionExpiredShown = true;
          
          messageService.add({
            severity: 'warn',
            summary: 'Sesión Expirada',
            detail: 'Su sesión ha expirado. Por favor, inicie sesión nuevamente.',
            life: 3000
          });

          setTimeout(() => {
            sessionExpiredShown = false; // Resetear el flag
            router.navigate(['/login']);
          }, 1500);
        }
      }
      return throwError(() => error);
    })
  );
};
```

**Beneficio**: Ahora solo se muestra **un toast** incluso si hay múltiples peticiones fallando.

### ✅ 2. Eliminado Toast Duplicado en AuthService

```typescript
// auth.service.ts
private handleRefreshTokenError(): void {
  this.clearAuthData();
  // No mostrar toast aquí, lo maneja el interceptor
  // Solo redirigir al login
  const router = inject(Router);
  router.navigate(['/login']);
}
```

**Beneficio**: Solo el interceptor muestra el toast, no hay duplicación.

### ✅ 3. Validación Silenciosa del Token

```typescript
// auth.service.ts
private checkTokenValidity(): void {
  const token = this.getStoredToken();
  if (token) {
    // Validar token de manera silenciosa
    this.validateToken().subscribe({
      next: () => {
        this.getProfile().subscribe({
          error: () => {
            // Si falla cargar el perfil, limpiar datos silenciosamente
            this.clearAuthData();
          }
        });
      },
      error: () => {
        // Token inválido, limpiar datos silenciosamente (sin mostrar toast)
        this.clearAuthData();
      }
    });
  }
}
```

**Beneficio**: La validación automática no genera toasts molestos.

### ✅ 4. Manejo Silencioso de Errores en Catálogos

```typescript
// employee-management.component.ts
loadEmployeeTypes(): void {
  this.employeeService.getEmployeeTypes().subscribe({
    next: (types) => {
      this.employeeTypes.set(types);
    },
    error: (error) => {
      console.error('Error loading employee types:', error);
      // No mostrar error al usuario, estos son datos de catálogo opcionales
      this.employeeTypes.set([]);
    }
  });
}
```

**Beneficio**: Los errores de catálogos no molestan al usuario, solo se registran en consola.

## Resultado Final

### Antes 🔴
- 8+ toasts por cada navegación
- Usuario confundido
- Redirección forzada al dashboard
- Experiencia de usuario muy mala

### Después ✅
- **Solo 1 toast** si la sesión realmente expiró
- Mensaje claro: "Su sesión ha expirado. Por favor, inicie sesión nuevamente."
- Redirección ordenada al login
- Experiencia de usuario limpia

## Flujo Correcto Ahora

```
1. Usuario navega a /empleados
   ↓
2. Se intenta cargar el componente
   ↓
3. Se hacen peticiones HTTP (empleados, catálogos)
   ↓
4. Si token expirado (401):
   ↓
5. Interceptor detecta el 401
   ↓
6. Flag sessionExpiredShown = false?
   ├─ SÍ → Mostrar 1 toast + Redirigir a login
   └─ NO → No mostrar más toasts (ya se mostró uno)
   ↓
7. Usuario ve el mensaje y es redirigido a /login
```

## Prevención de Problemas Futuros

### 🛡️ Mejoras de Seguridad

1. **Token Expiration Handling**:
   - El interceptor captura todos los 401
   - El flag evita spam de mensajes
   - La redirección es automática pero suave (1.5s delay)

2. **Manejo de Errores por Capas**:
   - **Interceptor**: Maneja errores de autenticación globales
   - **Service**: Maneja errores de negocio
   - **Component**: Maneja errores de UI específicos

3. **Logging**:
   - Errores se registran en console para debugging
   - Usuario solo ve mensajes relevantes

### 📋 Checklist para Nuevos Componentes

Al crear componentes que hacen peticiones HTTP:

- [ ] Manejar errores de catálogos/datos opcionales de forma silenciosa
- [ ] No mostrar toasts para cada error de petición
- [ ] Dejar que el interceptor maneje errores 401
- [ ] Solo mostrar toasts para errores que el usuario debe ver

## Testing

Para probar que funciona correctamente:

### Escenario 1: Token Expirado
1. Iniciar sesión
2. Esperar a que expire el token (o borrarlo de sessionStorage)
3. Navegar a /empleados
4. **Verificar**: Solo aparece 1 toast y redirección a login

### Escenario 2: Sin Token
1. Borrar sessionStorage y localStorage
2. Intentar acceder a /empleados directamente
3. **Verificar**: Redirección inmediata a login sin toasts

### Escenario 3: Token Válido
1. Iniciar sesión correctamente
2. Navegar a diferentes rutas
3. **Verificar**: Todo funciona sin toasts de error

## Archivos Modificados

1. ✅ `src/app/interceptors/session-expiry.interceptor.ts`
   - Agregado flag `sessionExpiredShown`
   - Mejorada la condición para evitar duplicados

2. ✅ `src/app/services/auth.service.ts`
   - Eliminado toast duplicado en `handleRefreshTokenError()`
   - Validación silenciosa en `checkTokenValidity()`

3. ✅ `src/app/components/employee-management/employee-management.component.ts`
   - Manejo silencioso de errores en catálogos
   - Arrays vacíos como fallback

## Nota Importante: Endpoints de Catálogo

Los endpoints de catálogo (`/api/tipos-empleado`, `/api/tipos-persona`, `/api/tipos-documento`) **deben existir en el backend**. 

Si estos endpoints no existen aún, tienes dos opciones:

### Opción A: Crear los endpoints en el backend
```java
@RestController
@RequestMapping("/api")
public class CatalogoController {
    
    @GetMapping("/tipos-empleado")
    public ResponseEntity<List<TipoEmpleado>> getTiposEmpleado() {
        // Implementar
    }
    
    @GetMapping("/tipos-persona")
    public ResponseEntity<List<TipoPersona>> getTiposPersona() {
        // Implementar
    }
    
    @GetMapping("/tipos-documento")
    public ResponseEntity<List<TipoDocumento>> getTiposDocumento() {
        // Implementar
    }
}
```

### Opción B: Datos hardcodeados temporales (para desarrollo)
```typescript
// employee.service.ts
getEmployeeTypes(): Observable<EmployeeType[]> {
  // Datos temporales hasta que exista el endpoint
  return of([
    { id: 1, descripcion: 'Técnico' },
    { id: 2, descripcion: 'Administrativo' },
    { id: 3, descripcion: 'Gerente' }
  ]);
}
```

## Conclusión

El problema de múltiples toasts estaba causado por:
- Duplicación de mensajes (interceptor + service)
- Múltiples peticiones HTTP fallando simultáneamente
- Falta de flag para evitar spam de mensajes

Todas las causas han sido corregidas. ✅

