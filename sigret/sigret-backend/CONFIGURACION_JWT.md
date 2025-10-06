# Configuración JWT - Tiempos de Expiración

## ⚠️ Problema Detectado

**El token estaba configurado para expirar en solo 1 minuto**, causando errores de:
- "Access Denied"
- "Sesión Expirada"
- Desconexiones constantes

## ✅ Solución Implementada

### Configuración Actual (Desarrollo)

```yaml
jwt:
  expiration: 28800000        # 8 horas
  refresh-expiration: 604800000  # 7 días
```

| Token | Duración | Milisegundos | Uso |
|-------|----------|--------------|-----|
| **Access Token** | 8 horas | 28,800,000 ms | Acceso normal a la API |
| **Refresh Token** | 7 días | 604,800,000 ms | Renovar el access token |

### Tabla de Conversión de Tiempos

| Tiempo | Milisegundos | Uso Recomendado |
|--------|--------------|-----------------|
| 1 minuto | 60,000 | ❌ Demasiado corto (era el problema) |
| 5 minutos | 300,000 | ❌ Muy corto |
| 15 minutos | 900,000 | ⚠️ Corto (testing) |
| 30 minutos | 1,800,000 | ✅ Producción (conservador) |
| 1 hora | 3,600,000 | ✅ Producción (estándar) |
| 2 horas | 7,200,000 | ✅ Producción/Desarrollo |
| 8 horas | 28,800,000 | ✅ **Desarrollo (actual)** |
| 24 horas | 86,400,000 | ⚠️ Desarrollo extendido |
| 7 días | 604,800,000 | ✅ **Refresh Token (actual)** |
| 30 días | 2,592,000,000 | ⚠️ Refresh Token extendido |

---

## 📋 Configuraciones Recomendadas por Entorno

### 1. Desarrollo (Actual)
**Ideal para trabajar cómodamente sin interrupciones**
```yaml
jwt:
  expiration: 28800000        # 8 horas
  refresh-expiration: 604800000  # 7 días
```

**Ventajas:**
- No interrumpe el desarrollo
- Sesión dura toda la jornada laboral
- Fácil de testear

### 2. Producción (Recomendado)
**Balance entre seguridad y experiencia de usuario**
```yaml
jwt:
  expiration: 3600000         # 1 hora
  refresh-expiration: 86400000  # 24 horas
```

**Ventajas:**
- Mayor seguridad
- Refresh token automático cada hora
- Sesión total de 24 horas

### 3. Producción (Más Seguro)
**Para aplicaciones que requieren alta seguridad**
```yaml
jwt:
  expiration: 1800000         # 30 minutos
  refresh-expiration: 43200000  # 12 horas
```

**Ventajas:**
- Alta seguridad
- Tokens de corta duración
- Sesión total de 12 horas

### 4. Testing
**Para testing rápido de expiración**
```yaml
jwt:
  expiration: 900000          # 15 minutos
  refresh-expiration: 3600000   # 1 hora
```

---

## 🔄 ¿Cómo Funciona el Refresh Token?

### Flujo Normal

1. **Login**: Usuario recibe Access Token (8h) + Refresh Token (7d)
2. **Uso normal**: Access Token se envía en cada request
3. **Token expira**: Después de 8 horas, el Access Token ya no es válido
4. **Refresh**: Frontend usa el Refresh Token para obtener un nuevo Access Token
5. **Nuevo Access Token**: Usuario continúa sin necesidad de hacer login

### Implementación en Frontend

```typescript
// Interceptor para manejar tokens expirados
export class AuthInterceptor implements HttpInterceptor {
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Agregar token a cada request
    const token = this.authService.getToken();
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si el token expiró (401)
        if (error.status === 401) {
          return this.handle401Error(req, next);
        }
        return throwError(() => error);
      })
    );
  }
  
  private handle401Error(req: HttpRequest<any>, next: HttpHandler) {
    // Intentar refrescar el token
    return this.authService.refreshToken().pipe(
      switchMap((token: any) => {
        // Reintentar el request con el nuevo token
        req = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token.accessToken}`
          }
        });
        return next.handle(req);
      }),
      catchError((err) => {
        // Si el refresh también falla, desloguear
        this.authService.logout();
        return throwError(() => err);
      })
    );
  }
}
```

---

## 🛡️ Endpoint de Refresh Token (Backend)

El backend ya tiene implementado el endpoint para refrescar tokens:

```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta:**
```json
{
  "accessToken": "nuevo_token...",
  "refreshToken": "mismo_refresh_token...",
  "expiresIn": 28800000
}
```

---

## 🔍 Verificar Configuración Actual

Para verificar cuánto dura tu token:

### 1. En los logs del backend
Al iniciar la aplicación, verás:
```
JWT Configuration:
- Expiration: 28800000 ms (8 hours)
- Refresh Expiration: 604800000 ms (7 days)
```

### 2. En la respuesta de login
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 28800000  // ← Tiempo en milisegundos
}
```

### 3. Decodificando el token (jwt.io)
```json
{
  "sub": "admin",
  "iat": 1633024800,  // Issued At
  "exp": 1633053600   // Expiration (8 horas después)
}
```

---

## 🚨 Síntomas de Token Corto

Si el token está mal configurado, verás:
- ❌ "Session expired" muy rápidamente
- ❌ "Access Denied" sin razón aparente
- ❌ Usuario debe hacer login constantemente
- ❌ Frontend muestra errores 401 Unauthorized

**Solución:** Aumentar el tiempo de `jwt.expiration`

---

## 💡 Recomendaciones

### Para Desarrollo
- ✅ Token de 4-8 horas (comodidad)
- ✅ Refresh token de 7 días
- ✅ No implementar auto-logout por inactividad

### Para Producción
- ✅ Token de 30 minutos a 1 hora (seguridad)
- ✅ Refresh token de 24 horas
- ✅ Implementar refresh automático
- ✅ Implementar auto-logout por inactividad (15-30 min)
- ✅ HTTPS obligatorio
- ✅ Almacenar tokens en httpOnly cookies (más seguro que localStorage)

---

## 🔐 Seguridad Adicional

### 1. Rotar Refresh Tokens
Cada vez que se usa el refresh token, generar uno nuevo:
```java
// En AuthService
public LoginResponseDto refreshToken(String refreshToken) {
    // Validar refresh token
    // ...
    
    // Generar NUEVO access token Y refresh token
    String newAccessToken = jwtUtil.generateToken(userDetails);
    String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
    
    return new LoginResponseDto(newAccessToken, newRefreshToken, ...);
}
```

### 2. Blacklist de Tokens (Logout)
Mantener una lista de tokens invalidados:
```java
// Redis o base de datos
public void logout(String token) {
    Long expirationTime = jwtUtil.getExpirationTime();
    redisTemplate.opsForValue().set("blacklist:" + token, "true", expirationTime, TimeUnit.MILLISECONDS);
}
```

### 3. Vincular Token a IP/User-Agent
Agregar validación adicional:
```java
claims.put("userAgent", request.getHeader("User-Agent"));
claims.put("ipAddress", request.getRemoteAddr());
```

---

## 📝 Archivo de Configuración Completo

```yaml
# application-dev.yml (Desarrollo)
jwt:
  expiration: 28800000        # 8 horas
  refresh-expiration: 604800000  # 7 días

# application-prod.yml (Producción)
jwt:
  expiration: 3600000         # 1 hora
  refresh-expiration: 86400000  # 24 horas
```

---

## 🔄 Aplicar Cambios

Después de modificar `application.yml`:

1. **Reiniciar la aplicación** para que tome los nuevos valores
2. **Frontend debe hacer login nuevamente** para obtener un token con la nueva duración
3. **Verificar** en los logs que la configuración es correcta

```bash
# Reiniciar backend
./mvnw spring-boot:run
```

---

## ⏰ Cálculo Rápido de Milisegundos

```
Segundos × 1,000 = Milisegundos
Minutos × 60,000 = Milisegundos
Horas × 3,600,000 = Milisegundos
Días × 86,400,000 = Milisegundos
```

**Ejemplos:**
- 30 min = 30 × 60,000 = 1,800,000 ms
- 2 horas = 2 × 3,600,000 = 7,200,000 ms
- 1 día = 1 × 86,400,000 = 86,400,000 ms

