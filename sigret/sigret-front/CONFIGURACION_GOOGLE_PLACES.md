# Configuración de Google Places API para Gestión de Direcciones

## 📋 Resumen

El frontend ahora incluye integración completa con Google Places API para la gestión de direcciones de empleados y clientes. Esta integración permite seleccionar direcciones válidas con autocompletado y geocodificación automática.

## 🔑 Configuración de Google Places API

### 1. Obtener API Key de Google

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la **Places API** y **Maps JavaScript API**:
   - Navega a "APIs & Services" > "Library"
   - Busca "Places API" y habilítala
   - Busca "Maps JavaScript API" y habilítala
4. Crea una API Key:
   - Ve a "APIs & Services" > "Credentials"
   - Click en "Create Credentials" > "API Key"
   - Copia la API Key generada

### 2. Configurar Restricciones (Recomendado)

Para mayor seguridad, configura restricciones en tu API Key:

#### Restricciones de Aplicación:
- **Tipo**: HTTP referrers (websites)
- **Referrers permitidos**: 
  - `http://localhost:4200/*` (desarrollo)
  - `https://tu-dominio.com/*` (producción)

#### Restricciones de API:
- Limita la key solo a:
  - Places API
  - Maps JavaScript API

### 3. Configurar en el Frontend

Abre los archivos de environment y reemplaza `TU_API_KEY_DE_GOOGLE_AQUI` con tu API Key real:

#### Desarrollo (`src/environments/environment.ts`):
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  googleMapsApiKey: 'AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxx' // Tu API key aquí
};
```

#### Producción (`src/environments/environment.prod.ts`):
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://tu-api-backend.com/api',
  googleMapsApiKey: 'AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxx' // Tu API key de producción
};
```

> **⚠️ IMPORTANTE**: Nunca subas tus API keys al repositorio. Considera usar variables de entorno en el servidor de producción.

## 🎯 Uso de la Funcionalidad

### Gestión de Empleados con Direcciones

#### 1. Crear Empleado con Dirección

1. Click en el botón **"Nuevo Empleado"**
2. Completa los datos básicos del empleado
3. En la sección **"Direcciones"**, click en **"Agregar Dirección"**
4. Comienza a escribir en el campo de búsqueda
5. Selecciona una dirección de las sugerencias de Google
6. Opcionalmente, agrega:
   - Piso
   - Departamento
   - Observaciones
7. Marca como **"Dirección Principal"** si es necesario
8. Click en **"Agregar"** para agregar la dirección a la lista
9. Puedes agregar múltiples direcciones repitiendo los pasos 3-8
10. Click en **"Crear"** para guardar el empleado con sus direcciones

#### 2. Ver Detalles del Empleado

Hay dos formas de ver los detalles completos:

**Opción A: Click en la fila**
- Haz click en cualquier parte de la fila del empleado en la tabla
- Se abrirá un modal flotante con toda la información

**Opción B: Botón de ojo**
- Click en el botón con ícono de ojo (👁️) en la columna de acciones
- Se abrirá el mismo modal de detalles

El modal de detalles muestra:
- ✅ Información personal completa
- ✅ Información laboral
- ✅ Datos de usuario y roles
- ✅ **Todas las direcciones con detalle completo**
- ✅ Indicador de dirección principal
- ✅ Coordenadas geográficas

#### 3. Editar Empleado y Direcciones

**Desde la tabla:**
- Click en el botón de editar (✏️) en la columna de acciones

**Desde el modal de detalles:**
- Click en el botón **"Editar Empleado"** en el footer del modal

En el formulario de edición:
- Las direcciones existentes se cargan automáticamente
- Puedes eliminar direcciones con el botón de basura (🗑️)
- Puedes cambiar la dirección principal con el botón de estrella (⭐)
- Puedes agregar nuevas direcciones
- Al guardar, **todas las direcciones se reemplazan** con las del formulario

> **📝 Nota**: Según la implementación del backend, cuando actualizas un empleado con direcciones, las direcciones anteriores se eliminan y se crean las nuevas.

## 🔄 Integración con el Backend

### Estructura de Datos Enviada

Cuando creas o actualizas un empleado con direcciones, el frontend envía:

```typescript
{
  // Datos del empleado...
  "direcciones": [
    {
      "piso": "3",
      "departamento": "A",
      "esPrincipal": true,
      "observaciones": "Timbre roto",
      "googlePlacesData": {
        "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
        "formattedAddress": "Av. Corrientes 1234, Buenos Aires, Argentina",
        "geometry": {
          "location": {
            "lat": -34.603722,
            "lng": -58.381592
          }
        },
        "addressComponents": [
          // Componentes de la dirección...
        ]
      }
    }
  ]
}
```

El backend procesa automáticamente:
- ✅ Extracción de componentes de dirección (calle, número, ciudad, etc.)
- ✅ Geocodificación (coordenadas)
- ✅ Validación de dirección principal única
- ✅ Asociación con la persona del empleado

## 📝 Características Principales

### ✅ Funcionalidades Implementadas

1. **Autocompletado de Direcciones**
   - Usa Google Places API
   - Sugerencias en tiempo real
   - Búsqueda restringida a Argentina (configurable)

2. **Múltiples Direcciones**
   - Un empleado puede tener varias direcciones
   - Una debe ser marcada como principal
   - Sistema automático para gestionar dirección principal única

3. **Información Detallada**
   - Dirección formateada completa
   - Piso y departamento opcionales
   - Observaciones personalizables
   - Coordenadas geográficas

4. **Modal de Detalles**
   - Vista completa de información del empleado
   - Filas clickeables en la tabla
   - Todas las direcciones con desglose completo
   - Botón directo para editar

5. **Validación y UX**
   - Validación de selección desde Google Places
   - Mensajes claros de error y éxito
   - Interfaz intuitiva y moderna
   - Responsive design

## 🎨 Mejoras Visuales

- ✅ Filas clickeables con efecto hover
- ✅ Modal flotante moderno
- ✅ Iconos descriptivos (ubicación, usuario, etc.)
- ✅ Badges para estado y roles
- ✅ Diseño responsive
- ✅ Animaciones suaves

## ⚠️ Consideraciones Importantes

### Costos de Google Places API

Google Places API tiene costos asociados:
- Las primeras 200 requests por día son gratuitas
- Después se cobra por request (verifica precios actuales)
- Configura alertas de facturación en Google Cloud Console

### Límites y Restricciones

- Configura límites diarios en tu API Key
- Implementa caché si es necesario
- Considera usar restricciones geográficas

### Seguridad

- **NUNCA** subas las API keys al repositorio
- Usa variables de entorno en producción
- Configura restricciones de HTTP referrer
- Monitorea el uso de la API

## 🐛 Troubleshooting

### Error: "No se pudo cargar Google Maps API"

**Causa**: API Key incorrecta o no configurada

**Solución**:
1. Verifica que la API Key esté correctamente configurada en `environment.ts`
2. Asegúrate de que las APIs están habilitadas en Google Cloud Console
3. Verifica las restricciones de la API Key

### El autocompletado no funciona

**Causa**: Places API no habilitada o restricciones incorrectas

**Solución**:
1. Verifica que Places API esté habilitada en Google Cloud Console
2. Revisa las restricciones de HTTP referrer
3. Verifica la consola del navegador para errores específicos

### Las direcciones no se guardan

**Causa**: Backend no está recibiendo o procesando las direcciones

**Solución**:
1. Verifica que el backend esté funcionando correctamente
2. Revisa los logs del backend para errores
3. Verifica que el DTO de dirección coincida con el backend

## 📚 Documentación Adicional

- [Google Places API Documentation](https://developers.google.com/maps/documentation/places/web-service/overview)
- [Google Maps JavaScript API](https://developers.google.com/maps/documentation/javascript/overview)
- [PrimeNG Components](https://primeng.org/)

## 🔄 Próximos Pasos

Posibles mejoras futuras:
- [ ] Visualización de direcciones en mapa
- [ ] Cálculo de rutas entre direcciones
- [ ] Búsqueda de empleados por ubicación
- [ ] Exportación de direcciones a CSV/PDF
- [ ] Integración con servicios de geolocalización

---

**Última actualización**: Octubre 2025
**Versión**: 1.0.0

