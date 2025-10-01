# 🔒 Recomendaciones de Seguridad para SIGRET

## ⚠️ Estado Actual vs Recomendaciones

### **Implementación Actual (Mejorada):**
- ✅ Access token en `sessionStorage` (se limpia al cerrar navegador)
- ✅ Refresh token cifrado en `localStorage`
- ✅ Cifrado básico con XOR + Base64
- ✅ Limpieza automática de datos al cerrar sesión

### **Recomendaciones para Producción:**

## 1. 🍪 **HTTP-Only Cookies (MÁS SEGURO)**

### Backend - Configuración de Cookies:
```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
    // ... lógica de autenticación ...
    
    // Cookie para access token
    Cookie accessTokenCookie = new Cookie("access_token", token);
    accessTokenCookie.setHttpOnly(true);     // No accesible desde JavaScript
    accessTokenCookie.setSecure(true);       // Solo HTTPS
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(900);        // 15 minutos
    accessTokenCookie.setSameSite("Strict"); // Protección CSRF
    
    // Cookie para refresh token
    Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(604800);    // 7 días
    refreshTokenCookie.setSameSite("Strict");
    
    response.addCookie(accessTokenCookie);
    response.addCookie(refreshTokenCookie);
    
    return ResponseEntity.ok(responseWithoutTokens);
}
```

### Frontend - Sin Almacenamiento de Tokens:
```typescript
// Los tokens se manejan automáticamente por las cookies
// No necesitamos localStorage ni sessionStorage
```

## 2. 🛡️ **Headers de Seguridad**

### Backend - Security Headers:
```java
@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                // Content Security Policy
                response.setHeader("Content-Security-Policy", 
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
                
                // X-Frame-Options
                response.setHeader("X-Frame-Options", "DENY");
                
                // X-Content-Type-Options
                response.setHeader("X-Content-Type-Options", "nosniff");
                
                // X-XSS-Protection
                response.setHeader("X-XSS-Protection", "1; mode=block");
                
                // Strict-Transport-Security (solo en HTTPS)
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                
                return true;
            }
        });
    }
}
```

## 3. 🔐 **Cifrado Avanzado**

### Usar librerías de criptografía real:
```bash
npm install crypto-js
```

```typescript
import CryptoJS from 'crypto-js';

export class SecureStorageService {
  private readonly SECRET_KEY = 'your-256-bit-secret-key';

  encrypt(data: string): string {
    return CryptoJS.AES.encrypt(data, this.SECRET_KEY).toString();
  }

  decrypt(encryptedData: string): string {
    const bytes = CryptoJS.AES.decrypt(encryptedData, this.SECRET_KEY);
    return bytes.toString(CryptoJS.enc.Utf8);
  }
}
```

## 4. 🔄 **Refresh Token Rotation**

### Backend - Rotar Refresh Tokens:
```java
public LoginResponseDto refreshToken(RefreshTokenRequestDto refreshRequest) {
    // ... validación ...
    
    // Generar NUEVO refresh token
    String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
    
    // Invalidar el refresh token anterior
    invalidateRefreshToken(refreshRequest.getRefreshToken());
    
    return new LoginResponseDto(newToken, newRefreshToken, expirationTime, userInfo);
}
```

## 5. 📱 **Detección de Dispositivos**

### Backend - Fingerprinting:
```java
@Entity
public class RefreshToken {
    private String token;
    private String deviceFingerprint;
    private String userAgent;
    private String ipAddress;
    private LocalDateTime expiresAt;
    private boolean revoked;
}
```

## 6. ⏰ **Timeouts y Límites**

### Configuración de Seguridad:
```yaml
# application.yml
security:
  jwt:
    access-token-expiration: 900000      # 15 minutos
    refresh-token-expiration: 604800000  # 7 días
    max-refresh-tokens-per-user: 3       # Máximo 3 dispositivos
    refresh-token-rotation: true         # Rotar refresh tokens
```

## 7. 🚨 **Monitoreo y Logs**

### Backend - Auditoría:
```java
@EventListener
public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
    auditService.logLogin(event.getAuthentication().getName(), getClientIP());
}

@EventListener
public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
    auditService.logFailedLogin(event.getAuthentication().getName(), getClientIP());
}
```

## 8. 🌐 **HTTPS Obligatorio**

### Configuración:
```yaml
# application.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

## 9. 🔍 **Validación de Tokens**

### Frontend - Validación Periódica:
```typescript
@Injectable()
export class TokenValidationService {
  validateTokenPeriodically(): void {
    setInterval(() => {
      if (this.authService.isAuthenticated()) {
        this.authService.validateToken().subscribe({
          error: () => this.authService.logout()
        });
      }
    }, 300000); // Cada 5 minutos
  }
}
```

## 10. 🧹 **Limpieza de Datos**

### Frontend - Limpieza Automática:
```typescript
@HostListener('window:beforeunload')
onBeforeUnload(): void {
  // Limpiar datos sensibles antes de cerrar
  this.secureStorage.clearSecureStorage();
}

@HostListener('window:focus')
onWindowFocus(): void {
  // Validar sesión cuando la ventana recupera el foco
  this.validateSession();
}
```

## 📊 **Comparación de Seguridad:**

| Método | XSS | CSRF | Persistencia | Acceso JS | Recomendación |
|--------|-----|------|--------------|-----------|---------------|
| localStorage | ❌ Vulnerable | ✅ OK | ❌ Persiste | ✅ Accesible | ⚠️ Básico |
| sessionStorage | ❌ Vulnerable | ✅ OK | ✅ Temporal | ✅ Accesible | ⚠️ Mejor |
| HTTP-Only Cookies | ✅ Seguro | ⚠️ Configurar | ⚠️ Configurable | ❌ No accesible | ✅ Óptimo |
| Memory + Cookies | ✅ Seguro | ⚠️ Configurar | ✅ Temporal | ❌ No accesible | ✅ Ideal |

## 🎯 **Implementación Recomendada:**

1. **Desarrollo**: Usar la implementación actual mejorada
2. **Staging**: Migrar a HTTP-Only cookies
3. **Producción**: HTTP-Only cookies + todas las medidas de seguridad

## 🚀 **Próximos Pasos:**

1. Implementar HTTP-Only cookies en el backend
2. Configurar headers de seguridad
3. Implementar refresh token rotation
4. Agregar auditoría y monitoreo
5. Configurar HTTPS en producción
