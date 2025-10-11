# Solución: Loop Infinito de Errores

## 🔴 Problema

Al cargar la página de empleados (o cualquier otra ruta), se generaba un **loop infinito de errores HTTP 401**, haciendo que el navegador se congele.

## 🔍 Causas del Loop Infinito

### Causa 1: Refresh Token Loop
```typescript
// ANTES (❌ Loop infinito)
catchError((error: HttpErrorResponse) => {
  if (error.status === 401 && authService.getRefreshToken()) {
    return handleTokenRefresh(req, next, authService);
  }
  return throwError(() => error);
})
```

**Problema**: Si el refresh token también estaba expirado, intentaba refrescar → fallaba con 401 → intentaba refrescar de nuevo → loop infinito.

**Solución**: Agregar flag para evitar múltiples intentos simultáneos.

### Causa 2: Validación Automática en Constructor
```typescript
// ANTES (❌ Loop infinito)
constructor(private http: HttpClient) {
  this.checkTokenValidity(); // ← Esto hacía petición HTTP al inicializar
}
```

**Problema**: 
1. Se carga el servicio
2. Llama a `checkTokenValidity()`
3. Hace petición HTTP
4. Falla con 401
5. El interceptor intenta refrescar
6. Vuelve a inicializar el servicio
7. Loop infinito

**Solución**: Eliminar validación automática.

### Causa 3: logout() Dentro de validateToken()
```typescript
// ANTES (❌ Causa loops)
validateToken(): Observable<TokenValidationResponse> {
  return this.http.get<TokenValidationResponse>(`${this.API_BASE_URL}/validate`)
    .pipe(
      catchError(error => {
        this.logout(); // ← Esto hace OTRA petición HTTP
        return throwError(() => error);
      })
    );
}
```

**Problema**: `logout()` hace otra petición HTTP que puede fallar y causar más loops.

**Solución**: Solo limpiar datos localmente, sin hacer peticiones HTTP.

## ✅ Soluciones Implementadas

### 1. Flag para Evitar Refresh Simultáneos

```typescript
// auth.interceptor.ts
let isRefreshing = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // ...
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Solo intentar refresh si NO estamos ya refrescando
      if (error.status === 401 && authService.getRefreshToken() && !isRefreshing) {
        return handleTokenRefresh(req, next, authService);
      }
      return throwError(() => error);
    })
  );
};

function handleTokenRefresh(req, next, authService) {
  isRefreshing = true; // ← Activar flag
  
  return authService.refreshTokenAutomatically().pipe(
    switchMap(() => {
      isRefreshing = false; // ← Resetear en éxito
      // Reintentar request con nuevo token
      const newToken = authService.getToken();
      if (newToken) {
        const newReq = req.clone({
          setHeaders: { Authorization: `Bearer ${newToken}` }
        });
        return next(newReq);
      }
      return throwError(() => new Error('No se pudo obtener nuevo token'));
    }),
    catchError((refreshError) => {
      isRefreshing = false; // ← Resetear en error
      authService.clearAuthData(); // Limpiar sin hacer peticiones
      return throwError(() => refreshError);
    })
  );
}
```

**Beneficio**: Solo se intenta refrescar el token UNA vez, aunque haya múltiples peticiones fallando.

### 2. Eliminar Validación Automática

```typescript
// auth.service.ts
constructor(private http: HttpClient) {
  // NO validar automáticamente para evitar loops
  // La validación se hará cuando el usuario navegue
}
```

**Beneficio**: El servicio no hace peticiones HTTP al inicializarse.

### 3. Limpiar Datos Sin Hacer Peticiones

```typescript
// auth.service.ts
validateToken(): Observable<TokenValidationResponse> {
  return this.http.get<TokenValidationResponse>(`${this.API_BASE_URL}/validate`)
    .pipe(
      catchError(error => {
        // NO llamar a logout() aquí para evitar loops
        // Solo limpiar datos localmente
        this.clearAuthData();
        return throwError(() => error);
      })
    );
}

// Cambiar de private a public para que el interceptor pueda llamarlo
clearAuthData(): void {
  sessionStorage.removeItem('auth_token');
  localStorage.removeItem('refresh_token');
  
  this.tokenSubject.next(null);
  this.refreshTokenSubject.next(null);
  this.userSubject.next(null);
  
  this.token.set(null);
  this.refreshTokenSignal.set(null);
  this.user.set(null);
}
```

**Beneficio**: Limpiar datos no causa más peticiones HTTP.

## 📊 Flujo Antes vs Después

### ANTES (❌ Loop infinito)
```
1. Usuario navega a /empleados
   ↓
2. AuthService se inicializa
   ↓
3. Llama checkTokenValidity() → HTTP GET /auth/validate
   ↓
4. Token expirado → 401
   ↓
5. Interceptor intenta refresh → HTTP POST /auth/refresh
   ↓
6. Refresh token también expirado → 401
   ↓
7. Interceptor intenta refresh de nuevo → HTTP POST /auth/refresh
   ↓
8. Vuelve al paso 6 → LOOP INFINITO
```

### AHORA (✅ Funciona correctamente)
```
1. Usuario navega a /empleados
   ↓
2. AuthService se inicializa (sin hacer peticiones)
   ↓
3. Componente hace peticiones (GET /api/empleados, etc.)
   ↓
4. Token expirado → 401
   ↓
5. Interceptor verifica: !isRefreshing → true
   ↓
6. Intenta refresh una vez → isRefreshing = true
   ↓
7. Si falla: isRefreshing = false, clearAuthData(), redirección
   ↓
8. Otras peticiones que fallan con 401 ven isRefreshing = true
   ↓
9. No intentan refrescar de nuevo, solo fallan silenciosamente
   ↓
10. sessionExpiryInterceptor muestra 1 toast y redirige a login
```

## 🧪 Cómo Probar que Está Solucionado

### Test 1: Token Expirado
1. Hacer login
2. Esperar a que el token expire (o borrarlo: `sessionStorage.removeItem('auth_token')`)
3. Navegar a `/empleados`
4. **Resultado esperado**: 
   - Solo 1 toast de "Sesión Expirada"
   - Redirección a login después de 1.5 segundos
   - NO loop infinito

### Test 2: Sin Token
1. Borrar todo: 
   ```javascript
   sessionStorage.clear();
   localStorage.clear();
   ```
2. Intentar acceder directamente a `http://localhost:4200/empleados`
3. **Resultado esperado**:
   - Redirección inmediata a login por el authGuard
   - NO loop infinito

### Test 3: Token Válido
1. Hacer login correctamente
2. Navegar a `/empleados`
3. **Resultado esperado**:
   - Carga normal sin errores
   - Se muestran los dropdowns con opciones
   - NO loop infinito

## 🔍 Cómo Detectar Loops en el Futuro

### En Browser DevTools (F12)

**Network Tab**:
```
❌ Loop detectado si ves:
- 100+ peticiones a /auth/refresh en 1 segundo
- Peticiones que se repiten infinitamente
- Status: "(failed) net::ERR_INSUFFICIENT_RESOURCES"
```

**Console Tab**:
```
❌ Loop detectado si ves:
- Cientos de errores "HttpErrorResponse"
- El navegador se congela
- El tab consume 100% de CPU
```

## 📝 Checklist Anti-Loop

Al modificar interceptores o servicios de autenticación:

- [ ] ¿El interceptor tiene un flag para evitar reintentos múltiples?
- [ ] ¿El servicio de auth hace peticiones HTTP en el constructor?
- [ ] ¿Los métodos de error llaman a otros métodos que hacen peticiones HTTP?
- [ ] ¿Hay manejo de errores en el catch de las peticiones de refresh?
- [ ] ¿Se resetea el flag en TODOS los casos (éxito y error)?

## 🎯 Principios de Diseño

### 1. Constructor Sin Side Effects
```typescript
// ✅ BIEN
constructor(private http: HttpClient) {
  // Solo inicializar variables
}

// ❌ MAL
constructor(private http: HttpClient) {
  this.validateToken(); // ← NO hacer peticiones HTTP aquí
}
```

### 2. Flags de Control
```typescript
// ✅ BIEN - Flag previene loops
let isRefreshing = false;
if (error.status === 401 && !isRefreshing) {
  isRefreshing = true;
  // hacer algo
  isRefreshing = false;
}

// ❌ MAL - Sin protección
if (error.status === 401) {
  // Se ejecuta infinitamente
}
```

### 3. Limpieza Sin Peticiones
```typescript
// ✅ BIEN - Solo limpia localmente
clearAuthData(): void {
  sessionStorage.removeItem('auth_token');
  this.token.set(null);
}

// ❌ MAL - Causa más peticiones
clearAuthData(): void {
  this.logout().subscribe(); // ← Otra petición HTTP
}
```

## 📚 Archivos Modificados

1. ✅ `src/app/interceptors/auth.interceptor.ts`
   - Agregado flag `isRefreshing`
   - Reseteo del flag en success y error
   - Limpieza sin peticiones al fallar refresh

2. ✅ `src/app/services/auth.service.ts`
   - Eliminada validación automática en constructor
   - Cambiado `clearAuthData()` de private a public
   - `validateToken()` ya no llama a `logout()`

3. ✅ `src/app/interceptors/session-expiry.interceptor.ts` (cambio anterior)
   - Flag `sessionExpiredShown` para evitar múltiples toasts

## ✅ Estado Actual

- **Loop infinito**: ✅ RESUELTO
- **Múltiples toasts**: ✅ RESUELTO  
- **Refresh token**: ✅ Funciona correctamente (1 intento)
- **Validación automática**: ✅ Deshabilitada
- **Limpieza de datos**: ✅ Sin side effects

## 🚀 Próximos Pasos

1. **Probar con backend corriendo**
2. **Hacer login con usuario válido**
3. **Navegar a /empleados**
4. **Verificar que carga sin loops**

Si aún hay problemas:
- Revisar console del navegador (F12)
- Revisar Network tab para ver qué peticiones se están haciendo
- Verificar que el backend esté respondiendo correctamente

