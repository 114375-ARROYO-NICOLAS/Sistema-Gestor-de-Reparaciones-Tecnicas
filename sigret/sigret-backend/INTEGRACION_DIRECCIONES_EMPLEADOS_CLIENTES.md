# Integración de Direcciones con Empleados y Clientes

## 📋 Resumen

Las direcciones están completamente integradas en la gestión de **Empleados** y **Clientes**. NO existe un menú independiente para direcciones, sino que se gestionan como parte del formulario de creación/actualización de empleados y clientes.

## 🎯 Funcionamiento

### Alta de Empleado/Cliente con Direcciones

Cuando se crea un **Empleado** o **Cliente**:
1. Se crea la **Persona**
2. Se crea el **Empleado/Cliente** asociado
3. **Automáticamente** se crean las **Direcciones** (si fueron proporcionadas en el formulario)

### Actualización de Empleado/Cliente con Direcciones

Cuando se actualiza un **Empleado** o **Cliente**:
1. Se actualizan los datos de la **Persona**
2. Se actualizan los datos del **Empleado/Cliente**
3. Si se envían direcciones, **se reemplazan** todas las direcciones existentes con las nuevas

### Consulta de Empleado/Cliente

Cuando se consulta un **Empleado** o **Cliente**, la respuesta **automáticamente incluye** todas las direcciones asociadas a esa persona.

---

## 💻 Ejemplos de Uso

### 1. Crear Empleado con Dirección (Google Places)

**Endpoint:** `POST /api/empleados`

```json
{
  "tipoEmpleadoId": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "tipoPersonaId": 1,
  "tipoDocumentoId": 1,
  "documento": "12345678",
  "sexo": "M",
  "rolUsuario": "TECNICO",
  "direcciones": [
    {
      "piso": "3",
      "departamento": "A",
      "esPrincipal": true,
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
          {
            "longName": "1234",
            "shortName": "1234",
            "types": ["street_number"]
          },
          {
            "longName": "Avenida Corrientes",
            "shortName": "Av. Corrientes",
            "types": ["route"]
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
          }
        ]
      }
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "nombreCompleto": "Juan Pérez",
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "tipoDocumento": "DNI",
  "tipoPersona": "Física",
  "sexo": "M",
  "tipoEmpleado": "Técnico",
  "tipoEmpleadoId": 1,
  "activo": true,
  "usuarioId": 1,
  "username": "12345678",
  "rolUsuario": "TECNICO",
  "usuarioActivo": true,
  "fechaCreacionUsuario": "2025-10-05T19:00:00",
  "ultimoLogin": null,
  "direcciones": [
    {
      "id": 1,
      "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
      "calle": "Avenida Corrientes",
      "numero": "1234",
      "ciudad": "Buenos Aires",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": true,
      "direccionCompleta": "Av. Corrientes 1234, Buenos Aires, Argentina, Piso 3, Depto. A",
      "latitud": -34.603722,
      "longitud": -58.381592
    }
  ]
}
```

### 2. Crear Cliente con Dirección (Forma Simplificada)

**Endpoint:** `POST /api/clientes`

```json
{
  "tipoPersona": {
    "id": 1,
    "descripcion": "Física"
  },
  "nombre": "María",
  "apellido": "González",
  "tipoDocumento": {
    "id": 1,
    "descripcion": "DNI"
  },
  "documento": "87654321",
  "sexo": "F",
  "comentarios": "Cliente VIP",
  "direcciones": [
    {
      "placeId": "ChIJ...",
      "direccionFormateada": "Calle Falsa 123, Springfield, Argentina",
      "latitud": -34.6037,
      "longitud": -58.3816,
      "calle": "Calle Falsa",
      "numero": "123",
      "ciudad": "Springfield",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": true
    }
  ]
}
```

### 3. Crear Empleado con Múltiples Direcciones

```json
{
  "tipoEmpleadoId": 2,
  "nombre": "Carlos",
  "apellido": "Rodríguez",
  "tipoPersonaId": 1,
  "tipoDocumentoId": 1,
  "documento": "11223344",
  "sexo": "M",
  "rolUsuario": "ADMINISTRATIVO",
  "direcciones": [
    {
      "calle": "Av. Libertador",
      "numero": "5000",
      "ciudad": "Buenos Aires",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": true,
      "googlePlacesData": {
        "placeId": "ChIJ...",
        "formattedAddress": "Av. Libertador 5000, Buenos Aires"
      }
    },
    {
      "calle": "Calle Secundaria",
      "numero": "100",
      "piso": "2",
      "departamento": "B",
      "ciudad": "La Plata",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": false,
      "observaciones": "Dirección alternativa"
    }
  ]
}
```

### 4. Actualizar Empleado y Reemplazar Direcciones

**Endpoint:** `PUT /api/empleados/1`

```json
{
  "nombre": "Juan Carlos",
  "apellido": "Pérez",
  "direcciones": [
    {
      "calle": "Nueva Calle",
      "numero": "999",
      "ciudad": "Córdoba",
      "provincia": "Córdoba",
      "pais": "Argentina",
      "esPrincipal": true
    }
  ]
}
```

**Nota:** Al actualizar con nuevas direcciones, las direcciones anteriores se **eliminan** y se crean las nuevas.

### 5. Consultar Empleado con Direcciones

**Endpoint:** `GET /api/empleados/1`

**Response:**
```json
{
  "id": 1,
  "nombreCompleto": "Juan Pérez",
  "nombre": "Juan",
  "apellido": "Pérez",
  "documento": "12345678",
  "tipoDocumento": "DNI",
  "tipoPersona": "Física",
  "sexo": "M",
  "tipoEmpleado": "Técnico",
  "activo": true,
  "usuarioId": 1,
  "username": "12345678",
  "rolUsuario": "TECNICO",
  "usuarioActivo": true,
  "direcciones": [
    {
      "id": 1,
      "placeId": "ChIJrTLr-GyuEmsRBfy61i59si0",
      "calle": "Avenida Corrientes",
      "numero": "1234",
      "ciudad": "Buenos Aires",
      "provincia": "Buenos Aires",
      "pais": "Argentina",
      "esPrincipal": true,
      "direccionCompleta": "Av. Corrientes 1234, Buenos Aires, Argentina",
      "latitud": -34.603722,
      "longitud": -58.381592
    }
  ]
}
```

---

## 🔄 Flujo de Datos

### Alta de Empleado/Cliente

```
Frontend
  ↓
  Formulario de Empleado/Cliente
  ↓
  Incluye campo de direcciones (con Google Places)
  ↓
  POST /api/empleados o /api/clientes
  ↓
Backend - EmpleadoService/ClienteService
  ↓
  1. Valida datos
  2. Crea Persona
  3. Crea Empleado/Cliente
  4. Crea Direcciones (si existen)
     ↓
     - Procesa Google Places automáticamente
     - Extrae coordenadas
     - Extrae componentes de dirección
     - Maneja dirección principal
  5. Guarda todo en transacción
  ↓
  Retorna DTO con direcciones incluidas
```

### Actualización de Empleado/Cliente

```
Frontend
  ↓
  Formulario de edición con direcciones
  ↓
  PUT /api/empleados/{id} o /api/clientes/{id}
  ↓
Backend - EmpleadoService/ClienteService
  ↓
  1. Busca Empleado/Cliente existente
  2. Actualiza datos de Persona
  3. Si vienen direcciones:
     ↓
     - Elimina TODAS las direcciones existentes
     - Crea las nuevas direcciones
     - Procesa Google Places
  4. Guarda todo en transacción
  ↓
  Retorna DTO actualizado con nuevas direcciones
```

---

## 🎨 Implementación en Frontend

### Formulario de Empleado/Cliente (React Example)

```jsx
import React, { useState, useRef, useEffect } from 'react';
import { Loader } from '@googlemaps/js-api-loader';

const EmpleadoForm = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    documento: '',
    tipoEmpleadoId: null,
    tipoPersonaId: null,
    tipoDocumentoId: null,
    sexo: '',
    rolUsuario: '',
    direcciones: []
  });

  const [direccionActual, setDireccionActual] = useState({
    piso: '',
    departamento: '',
    esPrincipal: false,
    googlePlacesData: null
  });

  const autocompleteRef = useRef(null);

  // Inicializar Google Places
  useEffect(() => {
    const loader = new Loader({
      apiKey: "TU_API_KEY",
      version: "weekly",
      libraries: ["places"]
    });

    loader.load().then(() => {
      if (autocompleteRef.current) {
        const autocomplete = new google.maps.places.Autocomplete(
          autocompleteRef.current,
          {
            componentRestrictions: { country: "ar" },
            fields: ["place_id", "formatted_address", "geometry", "address_components"]
          }
        );

        autocomplete.addListener("place_changed", () => {
          const place = autocomplete.getPlace();
          
          if (place.place_id) {
            setDireccionActual(prev => ({
              ...prev,
              googlePlacesData: {
                placeId: place.place_id,
                formattedAddress: place.formatted_address,
                geometry: {
                  location: {
                    lat: place.geometry?.location?.lat(),
                    lng: place.geometry?.location?.lng()
                  }
                },
                addressComponents: place.address_components?.map(comp => ({
                  longName: comp.long_name,
                  shortName: comp.short_name,
                  types: comp.types
                })) || []
              }
            }));
          }
        });
      }
    });
  }, []);

  const agregarDireccion = () => {
    if (direccionActual.googlePlacesData) {
      setFormData(prev => ({
        ...prev,
        direcciones: [...prev.direcciones, direccionActual]
      }));

      // Limpiar formulario de dirección
      setDireccionActual({
        piso: '',
        departamento: '',
        esPrincipal: false,
        googlePlacesData: null
      });
      autocompleteRef.current.value = '';
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch('/api/empleados', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        const empleado = await response.json();
        console.log('Empleado creado con direcciones:', empleado);
        // Redirigir o mostrar mensaje de éxito
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Campos básicos del empleado */}
      <input
        type="text"
        placeholder="Nombre"
        value={formData.nombre}
        onChange={e => setFormData({...formData, nombre: e.target.value})}
      />
      <input
        type="text"
        placeholder="Apellido"
        value={formData.apellido}
        onChange={e => setFormData({...formData, apellido: e.target.value})}
      />
      {/* ...más campos... */}

      {/* Sección de Direcciones */}
      <div className="direcciones-section">
        <h3>Agregar Dirección</h3>
        
        <input
          ref={autocompleteRef}
          type="text"
          placeholder="Buscar dirección con Google Places..."
          className="form-control"
        />

        <input
          type="text"
          placeholder="Piso (opcional)"
          value={direccionActual.piso}
          onChange={e => setDireccionActual({...direccionActual, piso: e.target.value})}
        />

        <input
          type="text"
          placeholder="Departamento (opcional)"
          value={direccionActual.departamento}
          onChange={e => setDireccionActual({...direccionActual, departamento: e.target.value})}
        />

        <label>
          <input
            type="checkbox"
            checked={direccionActual.esPrincipal}
            onChange={e => setDireccionActual({...direccionActual, esPrincipal: e.target.checked})}
          />
          Dirección Principal
        </label>

        <button type="button" onClick={agregarDireccion}>
          Agregar Dirección
        </button>
      </div>

      {/* Lista de direcciones agregadas */}
      <div className="direcciones-lista">
        <h4>Direcciones Agregadas:</h4>
        {formData.direcciones.map((dir, index) => (
          <div key={index} className="direccion-item">
            <p>{dir.googlePlacesData?.formattedAddress}</p>
            <p>Piso: {dir.piso} - Depto: {dir.departamento}</p>
            {dir.esPrincipal && <span className="badge">Principal</span>}
            <button 
              type="button"
              onClick={() => {
                const newDirs = [...formData.direcciones];
                newDirs.splice(index, 1);
                setFormData({...formData, direcciones: newDirs});
              }}
            >
              Eliminar
            </button>
          </div>
        ))}
      </div>

      <button type="submit">Crear Empleado</button>
    </form>
  );
};

export default EmpleadoForm;
```

---

## 🔑 Puntos Clave

### 1. NO existe endpoint independiente de direcciones en el frontend
- Las direcciones se gestionan SOLO desde empleados y clientes
- El endpoint `/api/direcciones` existe pero es para uso interno o casos especiales

### 2. Direcciones se procesan automáticamente
- Google Places se procesa en el backend
- No necesitas enviar `personaId` cuando creas desde empleado/cliente
- Las coordenadas se extraen automáticamente

### 3. Dirección Principal
- Solo puede haber UNA dirección principal por persona
- Al marcar una como principal, las demás se desmarcan automáticamente

### 4. Actualización de Direcciones
- Al actualizar empleado/cliente con direcciones, las anteriores se **eliminan**
- Si no envías direcciones en el update, las existentes se mantienen

### 5. Transaccionalidad
- Todo se guarda en una transacción
- Si algo falla, se hace rollback completo

---

## 🚀 Ventajas de este Enfoque

1. **✅ Simplicidad**: Un solo formulario para todo
2. **✅ Consistencia**: Los datos siempre están sincronizados
3. **✅ Atomicidad**: Todo se crea/actualiza junto
4. **✅ UX mejorada**: El usuario no navega entre pantallas
5. **✅ Google Places integrado**: Direcciones validadas automáticamente

---

## 📝 Notas Importantes

- El campo `direcciones` es **opcional** en creación y actualización
- Si no envías direcciones, el empleado/cliente se crea sin direcciones (válido)
- Las direcciones se retornan **siempre** en las consultas GET
- La lista puede estar vacía si no tiene direcciones
- Google Places procesa automáticamente en el backend
- El frontend solo debe enviar los datos de Google Places tal como los recibe

---

## 🧪 Casos de Prueba

### Caso 1: Crear sin direcciones
✅ **Válido** - Se crea empleado/cliente sin direcciones

### Caso 2: Crear con una dirección
✅ **Válido** - Se crea con una dirección asociada

### Caso 3: Crear con múltiples direcciones
✅ **Válido** - Se crean todas las direcciones

### Caso 4: Actualizar sin enviar direcciones
✅ **Válido** - Se actualizan datos, direcciones existentes se mantienen

### Caso 5: Actualizar con direcciones vacías
✅ **Válido** - Se eliminan todas las direcciones existentes

### Caso 6: Actualizar con nuevas direcciones
✅ **Válido** - Se reemplazan todas las direcciones

---

**Estado:** ✅ **IMPLEMENTADO Y FUNCIONAL**

