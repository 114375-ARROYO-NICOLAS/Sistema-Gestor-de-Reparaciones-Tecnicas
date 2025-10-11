# Optimización de Costos - Google Places API Sessions

## 💰 Resumen

Se ha implementado **gestión de sesiones de autocompletado** para optimizar el uso de Google Places API y reducir significativamente los costos de facturación.

## 📊 ¿Cómo Funciona el Modelo de Precios?

### Sin Sesiones (❌ Costoso):
- **Se cobra por cada solicitud de autocompletado**
- Si el usuario escribe "Avenida Corrientes 1234", se hacen ~20 solicitudes
- Cada solicitud se factura individualmente
- **Costo total**: 20 solicitudes × precio por solicitud

### Con Sesiones (✅ Optimizado):
- **Se cobra por sesión completa** si el usuario selecciona un lugar
- Múltiples solicitudes de autocompletado se agrupan en una sesión
- La sesión termina cuando el usuario selecciona un lugar
- **Costo total**: 1 sesión (sin importar cuántas teclas presionó)

### Ejemplo Real:

```
Usuario escribe: "Av. Corrientes 1234, Buenos Aires"

SIN Sesiones:
- "A" → 1 solicitud
- "Av" → 1 solicitud  
- "Av. C" → 1 solicitud
- "Av. Co" → 1 solicitud
... (20 solicitudes totales)
= Se facturan 20 solicitudes

CON Sesiones:
- "A" → parte de sesión
- "Av" → parte de sesión
- "Av. C" → parte de sesión
- "Av. Co" → parte de sesión
... (mismas 20 solicitudes)
= Se factura 1 sesión

💰 AHORRO: ~95% en este escenario
```

## 🔧 Implementación

### 1. Nueva API (PlaceAutocompleteElement)

La nueva API **gestiona sesiones automáticamente**:

```typescript
// No se requiere código adicional
this.placeAutocomplete = new google.maps.places.PlaceAutocompleteElement({
  componentRestrictions: { country: 'ar' },
  fields: ['place_id', 'formatted_address', 'geometry', 'address_components']
});

// Las sesiones se manejan internamente
// Google optimiza automáticamente la facturación
```

**Ventajas:**
- ✅ Cero configuración manual
- ✅ Siempre optimizado
- ✅ No hay que preocuparse por tokens

### 2. API Legacy (Autocomplete)

Para la API legacy, **gestionamos sesiones manualmente**:

```typescript
// 1. Crear token de sesión
private sessionToken: any = null;
private sessionTokenExpiryTime: number = 0;
private readonly SESSION_TOKEN_LIFETIME = 3 * 60 * 1000; // 3 minutos

// 2. Obtener o crear token
private getOrCreateSessionToken(): any {
  const now = Date.now();
  
  // Reusar token existente si aún es válido
  if (this.sessionToken && now < this.sessionTokenExpiryTime) {
    return this.sessionToken;
  }
  
  // Crear nuevo token (válido por 3 minutos)
  this.sessionToken = new google.maps.places.AutocompleteSessionToken();
  this.sessionTokenExpiryTime = now + this.SESSION_TOKEN_LIFETIME;
  return this.sessionToken;
}

// 3. Pasar token al crear Autocomplete
this.placeAutocomplete = new google.maps.places.Autocomplete(input, {
  componentRestrictions: { country: 'ar' },
  fields: [...],
  sessionToken: sessionToken  // ← Token de sesión
});

// 4. Limpiar token después de selección
private clearSessionToken(): void {
  this.sessionToken = null;
  this.sessionTokenExpiryTime = 0;
}
```

## 🎯 Ciclo de Vida de una Sesión

### 1. **Inicio de Sesión**
```
Usuario hace click en "Agregar Dirección"
  ↓
Se crea un nuevo SessionToken
  ↓
Token válido por 3 minutos
  ↓
Token se pasa a todas las solicitudes de autocompletado
```

### 2. **Durante la Sesión**
```
Usuario escribe en el campo
  ↓
"A" → solicitud con sessionToken
"Av" → solicitud con mismo sessionToken
"Av. C" → solicitud con mismo sessionToken
...
  ↓
Todas las solicitudes usan el mismo token
```

### 3. **Fin de Sesión (Exitoso)**
```
Usuario selecciona un lugar de las sugerencias
  ↓
Se obtienen los detalles del lugar
  ↓
SessionToken se marca como "completado"
  ↓
Google factura 1 sesión completa
  ↓
Token se limpia (clearSessionToken)
```

### 4. **Fin de Sesión (Sin Selección)**
```
Usuario cierra el formulario sin seleccionar
  ↓
SessionToken se limpia manualmente
  ↓
Solicitudes individuales se facturan por separado
  ↓
Token expirado después de 3 minutos
```

## 📝 Logs de Monitoreo

La implementación incluye logs detallados para monitorear el uso:

### Logs de Nueva API:
```
🆕 Using new PlaceAutocompleteElement API with automatic session management
✅ Google Places PlaceAutocompleteElement initialized
💰 Session management: Automatic (built-in cost optimization)
✅ Place selected (new API): Av. Corrientes 1234...
💰 Session completed successfully (billed as one session)
```

### Logs de API Legacy:
```
⬇️ Falling back to legacy Autocomplete API with manual session tokens
🎫 Created new session token (valid for 3 minutes)
✅ Google Places legacy Autocomplete initialized
💰 Session management: Manual (session token applied for cost optimization)
♻️ Reusing existing session token  ← Reutilizando token
✅ Place selected (legacy API): Av. Corrientes 1234...
🗑️ Session token cleared after place selection
💰 Session completed successfully (billed as one session)
```

### Logs de Limpieza:
```
⚠️ Closing address form without selection - clearing session token
```

## 🔍 Validación del Token

El sistema valida automáticamente la vigencia del token:

```typescript
private getOrCreateSessionToken(): any {
  const now = Date.now();
  
  // ✅ Token válido - reutilizar
  if (this.sessionToken && now < this.sessionTokenExpiryTime) {
    console.log('♻️ Reusing existing session token');
    return this.sessionToken;
  }
  
  // ❌ Token expirado o no existe - crear nuevo
  this.sessionToken = new google.maps.places.AutocompleteSessionToken();
  this.sessionTokenExpiryTime = now + this.SESSION_TOKEN_LIFETIME;
  console.log('🎫 Created new session token (valid for 3 minutes)');
  return this.sessionToken;
}
```

### ¿Por qué 3 minutos?

- Es el tiempo máximo recomendado por Google
- Tiempo suficiente para que el usuario complete la búsqueda
- Previene tokens "zombie" que nunca se completan
- Balance entre UX y optimización de costos

## 💡 Escenarios de Uso

### Escenario 1: Usuario Exitoso
```
1. Abre formulario → Crea token
2. Escribe "Av. Corrientes 1234" → Usa mismo token
3. Selecciona dirección → Completa sesión, limpia token
4. Agrega otra dirección → Crea nuevo token
5. Selecciona otra dirección → Completa sesión, limpia token

Resultado: 2 sesiones facturadas (óptimo)
```

### Escenario 2: Usuario Indeciso
```
1. Abre formulario → Crea token
2. Escribe "Av. Corrientes" → Usa token
3. Cierra formulario sin seleccionar → Limpia token
4. Reabre formulario → Crea nuevo token  
5. Escribe "Calle Florida 100" → Usa nuevo token
6. Selecciona dirección → Completa sesión, limpia token

Resultado: 
- Primera búsqueda: Solicitudes individuales (no hay selección)
- Segunda búsqueda: 1 sesión facturada
```

### Escenario 3: Token Expirado
```
1. Abre formulario → Crea token (válido hasta T+3min)
2. Usuario se distrae 5 minutos
3. Vuelve y escribe → Token expirado, crea nuevo token
4. Selecciona dirección → Completa sesión con nuevo token

Resultado: 1 sesión facturada (el primer token expiró sin uso)
```

## 📊 Estimación de Ahorro

### Sin Optimización:
```
Usuarios por día: 100
Búsquedas promedio por usuario: 2
Teclas promedio por búsqueda: 20
Solicitudes por día: 100 × 2 × 20 = 4,000 solicitudes

Costo (ejemplo): 4,000 × $0.017 = $68/día
Costo mensual: $68 × 30 = $2,040/mes
```

### Con Optimización (90% exitosas):
```
Usuarios por día: 100
Búsquedas promedio por usuario: 2
Sesiones completadas: 100 × 2 × 0.9 = 180 sesiones
Solicitudes sin selección: 100 × 2 × 0.1 × 20 = 400 solicitudes

Costo sesiones: 180 × $0.017 = $3.06/día
Costo solicitudes: 400 × $0.0032 = $1.28/día
Costo total: $4.34/día
Costo mensual: $4.34 × 30 = $130/mes

💰 AHORRO: $1,910/mes (93.6%)
```

> **Nota**: Los precios son aproximados y pueden variar según tu plan de Google Cloud.

## ✅ Mejores Prácticas Implementadas

### 1. **Reutilización de Tokens**
- ✅ Un token se reutiliza durante 3 minutos
- ✅ Previene crear tokens innecesarios
- ✅ Logs claros cuando se reutiliza

### 2. **Limpieza Automática**
- ✅ Token se limpia después de selección exitosa
- ✅ Token se limpia si se cierra sin seleccionar
- ✅ Token expira automáticamente después de 3 minutos

### 3. **Doble API Support**
- ✅ PlaceAutocompleteElement: Gestión automática
- ✅ Autocomplete legacy: Gestión manual con tokens
- ✅ Ambos optimizados para costos

### 4. **Monitoreo y Logs**
- ✅ Logs claros de creación de token
- ✅ Logs de reutilización de token
- ✅ Logs de finalización de sesión
- ✅ Logs de limpieza

## 🚫 Anti-Patterns Evitados

### ❌ NO HACER:
```typescript
// Crear nuevo token en cada búsqueda
function search() {
  const token = new AutocompleteSessionToken(); // ❌ Malo
  // Esto crea sesiones innecesarias
}

// No limpiar tokens después de uso
function onPlaceSelected() {
  // ... procesar lugar ...
  // ❌ Olvidar limpiar token
}

// Tokens de larga duración
const TOKEN_LIFETIME = 60 * 60 * 1000; // ❌ 1 hora es demasiado
```

### ✅ HACER (Implementado):
```typescript
// Reutilizar token existente
function getOrCreateToken() {
  if (token && !isExpired()) return token; // ✅ Reutilizar
  return new AutocompleteSessionToken(); // ✅ Solo si es necesario
}

// Limpiar después de uso
function onPlaceSelected() {
  // ... procesar lugar ...
  clearSessionToken(); // ✅ Limpiar
}

// Lifetime apropiado
const TOKEN_LIFETIME = 3 * 60 * 1000; // ✅ 3 minutos es óptimo
```

## 🎓 Recursos Adicionales

- [Google Places API Pricing](https://developers.google.com/maps/documentation/places/web-service/usage-and-billing)
- [Autocomplete Sessions](https://developers.google.com/maps/documentation/javascript/places-autocomplete#session_tokens)
- [Cost Optimization Guide](https://developers.google.com/maps/documentation/places/web-service/optimize-cost)

## 📈 Monitoreo de Costos

### En Google Cloud Console:

1. Ve a **APIs & Services** → **Dashboard**
2. Selecciona **Places API**
3. Revisa métricas:
   - Requests per day
   - Autocomplete sessions
   - Places details requests
   - Cost breakdown

### Alertas Recomendadas:

```
- Alerta si cost > $50/día
- Alerta si requests > 5,000/día sin sesiones
- Alerta si tasa de éxito de sesiones < 70%
```

## 🎯 Conclusión

La implementación de sesiones de autocompletado:

- ✅ **Reduce costos en ~90-95%** para usuarios que completan búsquedas
- ✅ **No afecta la UX** - funciona igual para el usuario
- ✅ **Compatible con ambas APIs** - nueva y legacy
- ✅ **Gestión automática** - tokens se crean y limpian correctamente
- ✅ **Monitoring incluido** - logs detallados para auditoría
- ✅ **Producción lista** - probado y optimizado

**Estado**: ✅ **IMPLEMENTADO Y OPTIMIZADO**

---

**Fecha de Implementación**: Octubre 2025  
**Ahorro Estimado**: 90-95% en costos de API  
**Compatibilidad**: Google Maps JavaScript API v3.x

