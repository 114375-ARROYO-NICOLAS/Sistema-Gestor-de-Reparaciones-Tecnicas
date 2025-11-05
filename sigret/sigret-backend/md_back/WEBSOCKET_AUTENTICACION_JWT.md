# WebSocket con Autenticación JWT - ACTUALIZACIÓN IMPORTANTE

## ✅ Backend Actualizado con Seguridad

### Archivos Modificados/Creados:

1. **[WebSocketAuthInterceptor.java](../src/main/java/com/sigret/config/WebSocketAuthInterceptor.java)** ✅ NUEVO
   - Intercepta todas las conexiones WebSocket
   - Valida el token JWT antes de permitir la conexión
   - Extrae información del usuario (username, rol, empleadoId)
   - Establece la autenticación en el contexto de seguridad

2. **[WebSocketConfig.java](../src/main/java/com/sigret/config/WebSocketConfig.java)** ✅ ACTUALIZADO
   - Registra el interceptor de autenticación
   - Configura el canal de entrada para validar tokens

3. **[SecurityConfig.java](../src/main/java/com/sigret/config/SecurityConfig.java)** ✅ ACTUALIZADO
   - Permite el acceso inicial al endpoint `/ws-servicios/**`
   - La autenticación real se hace en el interceptor

---

## 🔐 Cómo Funciona

1. **Cliente intenta conectarse** al WebSocket
2. **Interceptor captura** la conexión en el comando CONNECT
3. **Busca el token JWT** en:
   - Header `Authorization: Bearer TOKEN`
   - Header `token: TOKEN` (alternativo)
4. **Valida el token** usando JwtUtil
5. **Si es válido**:
   - Extrae username, rol, empleadoId
   - Crea autenticación de Spring Security
   - Permite la conexión
6. **Si es inválido**:
   - Lanza excepción
   - Rechaza la conexión

---

## 📱 ACTUALIZACIÓN REQUERIDA EN EL FRONTEND

### Opción 1: Enviar Token en Headers de Conexión (RECOMENDADO)

**src/app/services/websocket.service.ts**
```typescript
import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ServicioEvent } from '../models/servicio.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private servicioEventSubject = new BehaviorSubject<ServicioEvent | null>(null);
  public servicioEvent$: Observable<ServicioEvent | null> = this.servicioEventSubject.asObservable();
  private isConnected = false;

  constructor() {
    this.initializeClient();
  }

  private initializeClient(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-servicios'),
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      // ⭐ IMPORTANTE: Configurar headers de conexión con JWT
      connectHeaders: this.getConnectHeaders(),
    });

    this.client.onConnect = (frame) => {
      console.log('✅ Connected to WebSocket:', frame);
      this.isConnected = true;

      this.client.subscribe('/topic/servicios', (message: IMessage) => {
        const event: ServicioEvent = JSON.parse(message.body);
        console.log('📨 Evento recibido:', event);
        this.servicioEventSubject.next(event);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('❌ STOMP error:', frame);
      this.isConnected = false;
    };

    this.client.onWebSocketClose = () => {
      console.log('🔌 WebSocket connection closed');
      this.isConnected = false;
    };
  }

  // ⭐ Obtener el token JWT del localStorage
  private getJwtToken(): string | null {
    // Ajusta 'token' según el nombre que uses en tu AuthService
    return localStorage.getItem('token');
  }

  // ⭐ Preparar headers con el token JWT
  private getConnectHeaders(): any {
    const token = this.getJwtToken();
    if (token) {
      return {
        'Authorization': `Bearer ${token}`
      };
    }
    console.warn('⚠️ No se encontró token JWT para WebSocket');
    return {};
  }

  connect(): void {
    if (!this.isConnected && !this.client.active) {
      // Actualizar headers antes de conectar (por si el token cambió)
      this.client.connectHeaders = this.getConnectHeaders();
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.isConnected || this.client.active) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  getConnectionStatus(): boolean {
    return this.isConnected;
  }

  // ⭐ Reconectar con nuevo token (útil después de refrescar el token)
  reconnect(): void {
    console.log('🔄 Reconnecting WebSocket with new token...');
    this.disconnect();
    setTimeout(() => {
      this.initializeClient();
      this.connect();
    }, 500);
  }
}
```

### Opción 2: Enviar Token como Query Parameter (Alternativa)

Si por alguna razón no puedes enviar headers, puedes usar query parameters:

```typescript
private initializeClient(): void {
  const token = this.getJwtToken();
  const url = `http://localhost:8080/ws-servicios${token ? '?token=' + token : ''}`;

  this.client = new Client({
    webSocketFactory: () => new SockJS(url),
    // ... resto igual
  });
}
```

Sin embargo, **NO recomiendo** esta opción porque:
- El token queda expuesto en la URL
- Puede quedar registrado en logs
- Menos seguro

---

## 🧪 Testing

### 1. Probar sin Token (Debe Fallar)

```typescript
// Temporalmente comenta el código que agrega headers
connectHeaders: {}  // Sin token
```

**Resultado esperado:**
- ❌ Conexión rechazada
- Console: "Token JWT inválido o no presente"

### 2. Probar con Token Válido (Debe Funcionar)

```typescript
connectHeaders: {
  'Authorization': 'Bearer tu-token-jwt'
}
```

**Resultado esperado:**
- ✅ Conexión exitosa
- Console: "WebSocket autenticado exitosamente para usuario: [username] con rol: [rol]"
- Recibe eventos en tiempo real

### 3. Probar con Token Expirado (Debe Fallar)

- Usa un token antiguo
- Debería rechazar la conexión

### 4. Probar Reconexión después de Refresh Token

```typescript
// En tu AuthService, después de refrescar el token:
this.webSocketService.reconnect();
```

---

## 🔒 Seguridad Implementada

### Backend:
- ✅ WebSocket requiere JWT válido
- ✅ Token validado antes de establecer conexión
- ✅ Información del usuario extraída y disponible
- ✅ Roles verificados (PROPIETARIO, ADMINISTRATIVO, TECNICO)
- ✅ Contexto de seguridad establecido

### Qué Usuarios Pueden Conectarse:
- ✅ Cualquier usuario autenticado con token válido
- ✅ Todos los roles pueden ver el tablero
- ✅ Los permisos de los endpoints REST siguen aplicándose

---

## 📊 Logs del Backend

Cuando un cliente se conecta exitosamente, verás:

```
WebSocket autenticado exitosamente para usuario: admin con rol: PROPIETARIO
```

Cuando falla la autenticación:

```
WebSocket: Token JWT inválido o no presente
```

---

## 🐛 Troubleshooting

### Problema: "Token JWT inválido o no presente"

**Soluciones:**
1. Verifica que estás enviando el header `Authorization`
2. Asegúrate de que el token esté en el formato: `Bearer TOKEN`
3. Verifica que el token no esté expirado
4. Comprueba que el token esté guardado en localStorage

### Problema: "Connection refused" o "401 Unauthorized"

**Soluciones:**
1. Verifica que el backend esté corriendo
2. Revisa que `/ws-servicios/**` esté permitido en SecurityConfig
3. Comprueba los logs del backend

### Problema: Conexión exitosa pero no recibe eventos

**Soluciones:**
1. Verifica que estás suscrito a `/topic/servicios`
2. Comprueba que el backend esté emitiendo eventos
3. Revisa la consola del navegador para errores

---

## 🎯 Checklist de Implementación

### Backend ✅
- [x] WebSocketAuthInterceptor creado
- [x] WebSocketConfig actualizado con interceptor
- [x] SecurityConfig permite `/ws-servicios/**`
- [x] JwtUtil tiene método `validateToken(String token)`

### Frontend 🔲
- [ ] Instalar dependencias: `npm install @stomp/stompjs sockjs-client`
- [ ] Actualizar WebSocketService con connectHeaders
- [ ] Verificar nombre del token en localStorage
- [ ] Implementar método reconnect() para refresh token
- [ ] Probar conexión con token válido
- [ ] Probar rechazo con token inválido

---

## 💡 Mejoras Futuras (Opcionales)

1. **Manejo de Desconexión por Token Expirado:**
   - Detectar cuando el token expira
   - Refrescar automáticamente
   - Reconectar el WebSocket

2. **Notificaciones Personalizadas:**
   - Usar el `empleadoId` para enviar notificaciones específicas
   - Filtrar eventos por rol del usuario

3. **Métricas de Conexión:**
   - Tiempo de conexión activa
   - Cantidad de eventos recibidos
   - Estado de la conexión en tiempo real

4. **Heartbeat Personalizado:**
   - Ping/Pong para mantener conexión viva
   - Detección de pérdida de conexión

---

## 📚 Recursos

- [Spring WebSocket Security](https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html)
- [STOMP.js Documentation](https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html)
- [SockJS Client](https://github.com/sockjs/sockjs-client)

---

## ✅ Resumen

El WebSocket ahora está **completamente seguro** con autenticación JWT. Solo los usuarios autenticados pueden conectarse y recibir actualizaciones en tiempo real. La implementación es transparente para el frontend, solo necesitas agregar el token en los headers de conexión.

**Cualquier duda, revisa los comentarios en el código del WebSocketAuthInterceptor! 🚀**
