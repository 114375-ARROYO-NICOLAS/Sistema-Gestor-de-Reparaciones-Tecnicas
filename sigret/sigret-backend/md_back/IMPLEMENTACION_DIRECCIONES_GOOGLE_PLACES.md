# Implementación de Direcciones con Integración Google Places API

## Resumen

Se ha implementado exitosamente la entidad `Direccion` con **integración completa de Google Places API**. El sistema permite recibir datos directamente de Google Places desde el frontend, procesarlos automáticamente y almacenarlos de forma estructurada. Esto garantiza direcciones reales y minimiza el error humano.

## 🎯 Objetivo de la Integración

- **Direcciones Reales**: Usar Google Places API en el frontend para que el usuario seleccione direcciones validadas por Google
- **Reducir Errores**: Evitar errores de tipeo y direcciones inválidas
- **Geocodificación**: Almacenar coordenadas geográficas (latitud/longitud) automáticamente
- **Flexibilidad**: Permitir campos adicionales (piso, departamento) no incluidos en Google Places

## Arquitectura de la Solución

### Flujo de Trabajo

```
Frontend (con Google Places API)
    ↓
    Selecciona dirección en autocomplete de Google
    ↓
    Obtiene datos completos de Google Places
    ↓
    Envía al Backend (puede incluir datos adicionales como piso/depto)
    ↓
Backend (Spring Boot)
    ↓
    Recibe GooglePlacesDto
    ↓
    GooglePlacesParser extrae componentes
    ↓
    Se almacena en base de datos
```

## Componentes Implementados

### 1. Entidad Direccion (Actualizada)

**Archivo:** `src/main/java/com/sigret/entities/Direccion.java`

**Nuevos campos para Google Places:**
- `placeId` (String, único): ID único de Google Places para referencia
- `latitud` (Double): Coordenada de latitud
- `longitud` (Double): Coordenada de longitud
- `direccionFormateada` (String): Dirección completa formateada por Google

**Campos estructurados** (ahora opcionales):
- `calle`, `numero`, `piso`, `departamento`, `barrio`, `ciudad`, `provincia`, `codigoPostal`, `pais`

**Nuevo método:**
- `tieneUbicacion()`: Verifica si tiene coordenadas geográficas

### 2. DTOs de Google Places

#### GooglePlacesDto
**Archivo:** `src/main/java/com/sigret/dtos/direccion/GooglePlacesDto.java`

Modela la estructura que devuelve Google Places API:
- `placeId`: ID único del lugar
- `formattedAddress`: Dirección formateada completa
- `geometry.location`: Objeto con `lat` y `lng`
- `addressComponents`: Array con componentes de la dirección

**Estructura de ejemplo de Google Places:**
```json
{
  "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
  "formattedAddress": "Av. Libertador 1234, Buenos Aires, Argentina",
  "geometry": {
    "location": {
      "lat": -34.603722,
      "lng": -58.381592
    }
  },
  "addressComponents": [
    {
      "longName": "1234",
      "shortName": "1234",
      "types": ["street_number"]
    },
    {
      "longName": "Avenida Libertador",
      "shortName": "Av. Libertador",
      "types": ["route"]
    },
    {
      "longName": "Buenos Aires",
      "shortName": "Buenos Aires",
      "types": ["locality", "political"]
    }
    // ... más componentes
  ]
}
```

#### DTOs Actualizados

**DireccionCreateDto y DireccionUpdateDto:**
- Ahora incluyen todos los campos de Google Places
- Campo `googlePlacesData`: Objeto completo de Google Places (opcional)
- Prioridad: Los campos individuales sobrescriben los datos de `googlePlacesData`

**DireccionResponseDto y DireccionListDto:**
- Ahora incluyen `placeId`, `latitud`, `longitud`, `direccionFormateada`
- Campo `tieneUbicacion` en ResponseDto para saber si tiene coordenadas

### 3. Utilidad GooglePlacesParser

**Archivo:** `src/main/java/com/sigret/utilities/GooglePlacesParser.java`

**Métodos:**

#### `extractAddressComponents(GooglePlacesDto)`
Extrae componentes estructurados de la dirección:
- `street_number` → número
- `route` → calle
- `sublocality` / `neighborhood` → barrio
- `locality` → ciudad
- `administrative_area_level_1` → provincia
- `country` → país
- `postal_code` → código postal

#### `extractCoordinates(GooglePlacesDto)`
Extrae latitud y longitud del objeto geometry.location

### 4. Servicio Actualizado

**Archivo:** `src/main/java/com/sigret/services/impl/DireccionServiceImpl.java`

**Nuevo método privado: `procesarGooglePlacesData()`**
- Extrae Place ID
- Extrae dirección formateada
- Extrae coordenadas usando `GooglePlacesParser`
- Extrae componentes estructurados (calle, ciudad, etc.)
- Asigna todo a la entidad `Direccion`

**Lógica de prioridad:**
1. Si viene `googlePlacesData`, se procesa primero
2. Los campos individuales del DTO sobrescriben los datos de Google Places
3. Esto permite agregar piso/departamento después de seleccionar la dirección

## Ejemplos de Uso

### Caso 1: Enviar datos completos de Google Places

**Request:**
```json
POST /api/direcciones
{
  "personaId": 1,
  "piso": "5",
  "departamento": "B",
  "esPrincipal": true,
  "googlePlacesData": {
    "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
    "formattedAddress": "Av. Libertador 1234, Buenos Aires, Argentina",
    "geometry": {
      "location": {
        "lat": -34.603722,
        "lng": -58.381592
      }
    },
    "addressComponents": [
      {
        "longName": "1234",
        "shortName": "1234",
        "types": ["street_number"]
      },
      {
        "longName": "Avenida Libertador",
        "shortName": "Av. Libertador",
        "types": ["route"]
      },
      {
        "longName": "Recoleta",
        "shortName": "Recoleta",
        "types": ["neighborhood"]
      },
      {
        "longName": "Buenos Aires",
        "shortName": "Buenos Aires",
        "types": ["locality"]
      },
      {
        "longName": "Buenos Aires",
        "shortName": "BA",
        "types": ["administrative_area_level_1"]
      },
      {
        "longName": "Argentina",
        "shortName": "AR",
        "types": ["country"]
      },
      {
        "longName": "C1425",
        "shortName": "C1425",
        "types": ["postal_code"]
      }
    ]
  }
}
```

**El backend procesará automáticamente:**
- `placeId`: "ChIJrTLr-GyuEmsRBfy61i59si0"
- `direccionFormateada`: "Av. Libertador 1234, Buenos Aires, Argentina"
- `latitud`: -34.603722
- `longitud`: -58.381592
- `calle`: "Avenida Libertador"
- `numero`: "1234"
- `barrio`: "Recoleta"
- `ciudad`: "Buenos Aires"
- `provincia`: "Buenos Aires"
- `pais`: "Argentina"
- `codigoPostal`: "C1425"
- `piso`: "5" (del DTO)
- `departamento`: "B" (del DTO)

### Caso 2: Enviar datos procesados (forma simplificada)

Si el frontend ya procesó los datos de Google Places:

**Request:**
```json
POST /api/direcciones
{
  "personaId": 1,
  "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
  "direccionFormateada": "Av. Libertador 1234, Buenos Aires, Argentina",
  "latitud": -34.603722,
  "longitud": -58.381592,
  "calle": "Avenida Libertador",
  "numero": "1234",
  "barrio": "Recoleta",
  "ciudad": "Buenos Aires",
  "provincia": "Buenos Aires",
  "pais": "Argentina",
  "codigoPostal": "C1425",
  "piso": "5",
  "departamento": "B",
  "esPrincipal": true
}
```

### Caso 3: Ingreso manual (sin Google Places)

Aún se puede crear direcciones manualmente:

**Request:**
```json
POST /api/direcciones
{
  "personaId": 1,
  "calle": "Calle Falsa",
  "numero": "123",
  "ciudad": "Springfield",
  "provincia": "Springfield",
  "pais": "USA",
  "esPrincipal": false
}
```

## Response Completo

```json
{
  "id": 1,
  "personaId": 1,
  "nombrePersona": "Juan Pérez",
  "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
  "latitud": -34.603722,
  "longitud": -58.381592,
  "direccionFormateada": "Av. Libertador 1234, Buenos Aires, Argentina",
  "calle": "Avenida Libertador",
  "numero": "1234",
  "piso": "5",
  "departamento": "B",
  "barrio": "Recoleta",
  "ciudad": "Buenos Aires",
  "provincia": "Buenos Aires",
  "codigoPostal": "C1425",
  "pais": "Argentina",
  "observaciones": null,
  "esPrincipal": true,
  "direccionCompleta": "Av. Libertador 1234, Buenos Aires, Argentina, Piso 5, Depto. B",
  "tieneUbicacion": true
}
```

## Base de Datos

La tabla `direcciones` incluye los nuevos campos:

```sql
CREATE TABLE direcciones (
    id_direccion BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_persona BIGINT NOT NULL,
    
    -- Campos de Google Places
    place_id VARCHAR(255) UNIQUE,
    latitud DOUBLE,
    longitud DOUBLE,
    direccion_formateada VARCHAR(500),
    
    -- Campos estructurados (ahora opcionales)
    calle VARCHAR(200),
    numero VARCHAR(20),
    piso VARCHAR(10),
    departamento VARCHAR(10),
    barrio VARCHAR(200),
    ciudad VARCHAR(100),
    provincia VARCHAR(100),
    codigo_postal VARCHAR(20),
    pais VARCHAR(100),
    
    -- Otros campos
    observaciones VARCHAR(500),
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (id_persona) REFERENCES personas(id_persona)
);
```

## Ventajas de esta Implementación

### 1. ✅ Flexibilidad Total
- Soporta datos completos de Google Places API
- Soporta datos pre-procesados por el frontend
- Soporta ingreso manual tradicional
- Permite agregar piso/departamento a direcciones de Google Places

### 2. ✅ Geocodificación Automática
- Almacena coordenadas geográficas
- Útil para mapas, cálculo de distancias, zonas de servicio

### 3. ✅ Validación de Direcciones
- Google Places garantiza direcciones reales
- Reduce errores de tipeo
- Normaliza formatos de direcciones

### 4. ✅ Trazabilidad
- `placeId` permite verificar la dirección en Google Maps
- Se mantiene la dirección formateada por Google

### 5. ✅ Compatibilidad con Argentina
- Soporta pisos y departamentos (común en Argentina)
- Maneja barrios correctamente
- Soporta códigos postales argentinos

## Integración en el Frontend

### Recomendaciones para React/Angular/Vue

1. **Usar Google Places Autocomplete**
```javascript
// Ejemplo conceptual
const handlePlaceSelect = (place) => {
  const direccionData = {
    personaId: personaId,
    piso: pisoInput.value,
    departamento: deptoInput.value,
    esPrincipal: true,
    googlePlacesData: {
      placeId: place.place_id,
      formattedAddress: place.formatted_address,
      geometry: {
        location: {
          lat: place.geometry.location.lat(),
          lng: place.geometry.location.lng()
        }
      },
      addressComponents: place.address_components.map(comp => ({
        longName: comp.long_name,
        shortName: comp.short_name,
        types: comp.types
      }))
    }
  };
  
  // Enviar al backend
  await fetch('/api/direcciones', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(direccionData)
  });
};
```

2. **Campos adicionales en el formulario**
- Después de seleccionar en Google Places, mostrar campos para piso y departamento
- Agregar campo de observaciones

3. **Visualización**
- Mostrar la `direccionFormateada` + piso + departamento
- Mostrar mapa con las coordenadas si están disponibles

## Endpoints No Modificados

Todos los endpoints REST siguen funcionando igual:
- `POST /api/direcciones` - Crear (ahora acepta Google Places)
- `GET /api/direcciones/{id}` - Obtener por ID
- `GET /api/direcciones/persona/{personaId}` - Listar por persona
- `PUT /api/direcciones/{id}` - Actualizar (ahora acepta Google Places)
- `DELETE /api/direcciones/{id}` - Eliminar
- Y todos los demás endpoints...

## Compilación Exitosa

```bash
./mvnw clean compile
[INFO] BUILD SUCCESS
[INFO] Total time:  14.463 s
```

## Próximos Pasos Sugeridos

1. **Frontend**: Implementar Google Places Autocomplete
2. **Validaciones**: Agregar validación de que al menos `direccionFormateada` o `ciudad` existan
3. **Búsquedas Geográficas**: Implementar búsqueda por proximidad usando lat/lng
4. **Integración con Mapas**: Mostrar direcciones en mapas usando las coordenadas
5. **Histórico**: Guardar un histórico si una dirección cambia su `placeId`

## Documentación de Google Places API

Para el frontend, consultar:
- [Google Places Autocomplete](https://developers.google.com/maps/documentation/javascript/place-autocomplete)
- [Place Details](https://developers.google.com/maps/documentation/javascript/place-details)
- [Places API Web Service](https://developers.google.com/maps/documentation/places/web-service)

## Notas Técnicas

- **Place ID**: Es único y persistente para cada lugar en Google Places
- **Coordenadas**: Se almacenan en formato decimal (WGS84)
- **Parsing flexible**: El parser maneja diferentes tipos de componentes de dirección
- **Null-safe**: Todos los métodos manejan valores null correctamente
- **Transaccional**: Todas las operaciones son transaccionales para garantizar consistencia

