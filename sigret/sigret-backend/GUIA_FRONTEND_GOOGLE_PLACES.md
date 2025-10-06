# Guía de Integración Frontend - Google Places API

## 📋 Para el Equipo de Frontend

Esta guía explica cómo integrar Google Places API en el frontend y enviar los datos al backend para crear/actualizar direcciones.

## 🎯 Objetivo

Permitir que los usuarios seleccionen direcciones reales usando el autocompletado de Google Places, minimizando errores de escritura y garantizando direcciones válidas.

## 🔑 Setup Inicial

### 1. Obtener API Key de Google

1. Ir a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear o seleccionar un proyecto
3. Habilitar las APIs:
   - **Places API**
   - **Maps JavaScript API**
   - **Geocoding API** (opcional)
4. Crear credenciales (API Key)
5. Restringir la API key al dominio de tu aplicación

### 2. Instalar Dependencias

#### React
```bash
npm install @react-google-maps/api
```

#### Angular
```bash
npm install @angular/google-maps
```

#### Vue
```bash
npm install vue3-google-map
# o usar directamente el script de Google
```

#### Vanilla JavaScript
Agregar en el HTML:
```html
<script src="https://maps.googleapis.com/maps/api/js?key=TU_API_KEY&libraries=places"></script>
```

## 💻 Implementación

### Opción 1: Enviar Objeto Completo de Google Places (Recomendado)

Esta es la opción más simple. El backend procesará automáticamente todos los datos.

#### React Example

```tsx
import React, { useState, useRef, useEffect } from 'react';
import { Loader } from '@googlemaps/js-api-loader';

interface DireccionForm {
  personaId: number;
  piso?: string;
  departamento?: string;
  observaciones?: string;
  esPrincipal: boolean;
  googlePlacesData?: any;
}

const DireccionForm: React.FC = () => {
  const [form, setForm] = useState<DireccionForm>({
    personaId: 1, // Obtener del contexto o props
    esPrincipal: false
  });
  
  const autocompleteInputRef = useRef<HTMLInputElement>(null);
  const [autocomplete, setAutocomplete] = useState<google.maps.places.Autocomplete | null>(null);

  useEffect(() => {
    const loader = new Loader({
      apiKey: "TU_API_KEY",
      version: "weekly",
      libraries: ["places"]
    });

    loader.load().then(() => {
      if (autocompleteInputRef.current) {
        const autocompleteInstance = new google.maps.places.Autocomplete(
          autocompleteInputRef.current,
          {
            componentRestrictions: { country: "ar" }, // Restringir a Argentina
            fields: ["place_id", "formatted_address", "geometry", "address_components"]
          }
        );

        autocompleteInstance.addListener("place_changed", () => {
          const place = autocompleteInstance.getPlace();
          
          if (place.place_id) {
            // Convertir los datos de Google Places al formato que espera el backend
            const googlePlacesData = {
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
            };

            setForm(prev => ({ ...prev, googlePlacesData }));
          }
        });

        setAutocomplete(autocompleteInstance);
      }
    });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const response = await fetch('http://localhost:8080/api/direcciones', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(form)
      });

      if (response.ok) {
        const direccion = await response.json();
        console.log('Dirección creada:', direccion);
        // Mostrar mensaje de éxito, redirigir, etc.
      }
    } catch (error) {
      console.error('Error al crear dirección:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Buscar Dirección</label>
        <input
          ref={autocompleteInputRef}
          type="text"
          className="form-control"
          placeholder="Ingrese una dirección..."
        />
      </div>

      <div className="form-group">
        <label>Piso (opcional)</label>
        <input
          type="text"
          value={form.piso || ''}
          onChange={e => setForm({ ...form, piso: e.target.value })}
          className="form-control"
        />
      </div>

      <div className="form-group">
        <label>Departamento (opcional)</label>
        <input
          type="text"
          value={form.departamento || ''}
          onChange={e => setForm({ ...form, departamento: e.target.value })}
          className="form-control"
        />
      </div>

      <div className="form-group">
        <label>Observaciones (opcional)</label>
        <textarea
          value={form.observaciones || ''}
          onChange={e => setForm({ ...form, observaciones: e.target.value })}
          className="form-control"
        />
      </div>

      <div className="form-check">
        <input
          type="checkbox"
          checked={form.esPrincipal}
          onChange={e => setForm({ ...form, esPrincipal: e.target.checked })}
          className="form-check-input"
        />
        <label className="form-check-label">Marcar como dirección principal</label>
      </div>

      <button type="submit" className="btn btn-primary">
        Guardar Dirección
      </button>
    </form>
  );
};

export default DireccionForm;
```

#### Vue 3 Example

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <div class="form-group">
      <label>Buscar Dirección</label>
      <input
        ref="autocompleteInput"
        type="text"
        class="form-control"
        placeholder="Ingrese una dirección..."
      />
    </div>

    <div class="form-group">
      <label>Piso (opcional)</label>
      <input v-model="form.piso" type="text" class="form-control" />
    </div>

    <div class="form-group">
      <label>Departamento (opcional)</label>
      <input v-model="form.departamento" type="text" class="form-control" />
    </div>

    <div class="form-group">
      <label>Observaciones (opcional)</label>
      <textarea v-model="form.observaciones" class="form-control"></textarea>
    </div>

    <div class="form-check">
      <input
        v-model="form.esPrincipal"
        type="checkbox"
        class="form-check-input"
      />
      <label class="form-check-label">Marcar como dirección principal</label>
    </div>

    <button type="submit" class="btn btn-primary">Guardar Dirección</button>
  </form>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Loader } from '@googlemaps/js-api-loader';
import axios from 'axios';

const autocompleteInput = ref<HTMLInputElement | null>(null);
const form = ref({
  personaId: 1, // Obtener del store o props
  piso: '',
  departamento: '',
  observaciones: '',
  esPrincipal: false,
  googlePlacesData: null as any
});

onMounted(async () => {
  const loader = new Loader({
    apiKey: "TU_API_KEY",
    version: "weekly",
    libraries: ["places"]
  });

  await loader.load();

  if (autocompleteInput.value) {
    const autocomplete = new google.maps.places.Autocomplete(
      autocompleteInput.value,
      {
        componentRestrictions: { country: "ar" },
        fields: ["place_id", "formatted_address", "geometry", "address_components"]
      }
    );

    autocomplete.addListener("place_changed", () => {
      const place = autocomplete.getPlace();
      
      if (place.place_id) {
        form.value.googlePlacesData = {
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
        };
      }
    });
  }
});

const handleSubmit = async () => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/direcciones',
      form.value,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    
    console.log('Dirección creada:', response.data);
    // Mostrar mensaje de éxito, redirigir, etc.
  } catch (error) {
    console.error('Error al crear dirección:', error);
  }
};
</script>
```

#### Angular Example

```typescript
// direccion-form.component.ts
import { Component, OnInit, ViewChild, ElementRef, NgZone } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-direccion-form',
  templateUrl: './direccion-form.component.html'
})
export class DireccionFormComponent implements OnInit {
  @ViewChild('autocompleteInput') autocompleteInput!: ElementRef;

  form = {
    personaId: 1, // Obtener del servicio
    piso: '',
    departamento: '',
    observaciones: '',
    esPrincipal: false,
    googlePlacesData: null as any
  };

  constructor(
    private http: HttpClient,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.loadGooglePlaces();
  }

  private loadGooglePlaces() {
    const loader = new (window as any).google.maps.plugins.loader.Loader({
      apiKey: "TU_API_KEY",
      version: "weekly",
      libraries: ["places"]
    });

    loader.load().then(() => {
      const autocomplete = new google.maps.places.Autocomplete(
        this.autocompleteInput.nativeElement,
        {
          componentRestrictions: { country: 'ar' },
          fields: ['place_id', 'formatted_address', 'geometry', 'address_components']
        }
      );

      autocomplete.addListener('place_changed', () => {
        this.ngZone.run(() => {
          const place = autocomplete.getPlace();
          
          if (place.place_id) {
            this.form.googlePlacesData = {
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
            };
          }
        });
      });
    });
  }

  onSubmit() {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    this.http.post('http://localhost:8080/api/direcciones', this.form, { headers })
      .subscribe({
        next: (response) => {
          console.log('Dirección creada:', response);
          // Mostrar mensaje de éxito
        },
        error: (error) => {
          console.error('Error al crear dirección:', error);
        }
      });
  }
}
```

```html
<!-- direccion-form.component.html -->
<form (ngSubmit)="onSubmit()">
  <div class="form-group">
    <label>Buscar Dirección</label>
    <input
      #autocompleteInput
      type="text"
      class="form-control"
      placeholder="Ingrese una dirección..."
    />
  </div>

  <div class="form-group">
    <label>Piso (opcional)</label>
    <input [(ngModel)]="form.piso" name="piso" type="text" class="form-control" />
  </div>

  <div class="form-group">
    <label>Departamento (opcional)</label>
    <input [(ngModel)]="form.departamento" name="departamento" type="text" class="form-control" />
  </div>

  <div class="form-group">
    <label>Observaciones (opcional)</label>
    <textarea [(ngModel)]="form.observaciones" name="observaciones" class="form-control"></textarea>
  </div>

  <div class="form-check">
    <input
      [(ngModel)]="form.esPrincipal"
      name="esPrincipal"
      type="checkbox"
      class="form-check-input"
    />
    <label class="form-check-label">Marcar como dirección principal</label>
  </div>

  <button type="submit" class="btn btn-primary">Guardar Dirección</button>
</form>
```

### Opción 2: Procesar en Frontend y Enviar Datos Estructurados

Si prefieres procesar los datos en el frontend y enviar solo los campos necesarios:

```typescript
const handlePlaceChanged = (place: google.maps.places.PlaceResult) => {
  const direccionData = {
    personaId: 1,
    placeId: place.place_id,
    direccionFormateada: place.formatted_address,
    latitud: place.geometry?.location?.lat(),
    longitud: place.geometry?.location?.lng(),
    // Extraer componentes manualmente
    calle: extractComponent(place, 'route'),
    numero: extractComponent(place, 'street_number'),
    barrio: extractComponent(place, ['neighborhood', 'sublocality']),
    ciudad: extractComponent(place, 'locality'),
    provincia: extractComponent(place, 'administrative_area_level_1'),
    pais: extractComponent(place, 'country'),
    codigoPostal: extractComponent(place, 'postal_code'),
    piso: '', // Agregado por el usuario
    departamento: '', // Agregado por el usuario
    esPrincipal: false
  };

  // Enviar al backend
  await fetch('http://localhost:8080/api/direcciones', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(direccionData)
  });
};

// Función helper para extraer componentes
const extractComponent = (
  place: google.maps.places.PlaceResult,
  types: string | string[]
): string => {
  const typeArray = Array.isArray(types) ? types : [types];
  
  for (const type of typeArray) {
    const component = place.address_components?.find(comp =>
      comp.types.includes(type)
    );
    if (component) return component.long_name;
  }
  
  return '';
};
```

## 📊 Visualizar Direcciones

### Mostrar en Lista

```tsx
interface Direccion {
  id: number;
  direccionCompleta: string;
  esPrincipal: boolean;
  tieneUbicacion: boolean;
  latitud?: number;
  longitud?: number;
}

const DireccionesList: React.FC<{ personaId: number }> = ({ personaId }) => {
  const [direcciones, setDirecciones] = useState<Direccion[]>([]);

  useEffect(() => {
    fetch(`http://localhost:8080/api/direcciones/persona/${personaId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
      .then(res => res.json())
      .then(setDirecciones);
  }, [personaId]);

  return (
    <div>
      <h3>Direcciones</h3>
      {direcciones.map(dir => (
        <div key={dir.id} className={dir.esPrincipal ? 'principal' : ''}>
          <p>{dir.direccionCompleta}</p>
          {dir.esPrincipal && <span className="badge">Principal</span>}
          {dir.tieneUbicacion && (
            <a
              href={`https://www.google.com/maps?q=${dir.latitud},${dir.longitud}`}
              target="_blank"
              rel="noopener noreferrer"
            >
              Ver en mapa
            </a>
          )}
        </div>
      ))}
    </div>
  );
};
```

## 🗺️ Mostrar en Mapa

```tsx
import { GoogleMap, Marker, useLoadScript } from '@react-google-maps/api';

const DireccionMap: React.FC<{ latitud: number; longitud: number }> = ({ latitud, longitud }) => {
  const { isLoaded } = useLoadScript({
    googleMapsApiKey: "TU_API_KEY"
  });

  if (!isLoaded) return <div>Cargando mapa...</div>;

  return (
    <GoogleMap
      zoom={15}
      center={{ lat: latitud, lng: longitud }}
      mapContainerStyle={{ width: '100%', height: '400px' }}
    >
      <Marker position={{ lat: latitud, lng: longitud }} />
    </GoogleMap>
  );
};
```

## 🔒 Seguridad

### Incluir Token JWT

Todos los endpoints requieren autenticación:

```typescript
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('token')}`
};
```

### Restringir API Key de Google

En Google Cloud Console:
1. Ir a "Credenciales"
2. Editar la API Key
3. Restricciones de aplicación → Restringir por HTTP referrers
4. Agregar tu dominio: `https://tudominio.com/*`

## 📝 Validaciones

### En el Frontend

```typescript
const validateForm = () => {
  if (!form.googlePlacesData && !form.direccionFormateada) {
    alert('Por favor seleccione una dirección válida');
    return false;
  }
  return true;
};
```

## 🧪 Testing

### Datos de Prueba

Para testing sin llamar a Google Places API:

```typescript
const mockDireccion = {
  personaId: 1,
  placeId: "ChIJrTLr-GyuEmsRBfy61i59si0",
  direccionFormateada: "Av. Libertador 1234, Buenos Aires, Argentina",
  latitud: -34.603722,
  longitud: -58.381592,
  calle: "Avenida Libertador",
  numero: "1234",
  ciudad: "Buenos Aires",
  provincia: "Buenos Aires",
  pais: "Argentina",
  esPrincipal: true
};
```

## 📚 Recursos Adicionales

- [Google Places Autocomplete Docs](https://developers.google.com/maps/documentation/javascript/place-autocomplete)
- [Google Maps React](https://visgl.github.io/react-google-maps/)
- [Angular Google Maps](https://angular-maps.com/)
- [Vue Google Maps](https://github.com/xkjyeah/vue-google-maps)

## ❓ Preguntas Frecuentes

**Q: ¿Puedo seguir creando direcciones manualmente sin Google Places?**
A: Sí, todos los campos son opcionales excepto `personaId`.

**Q: ¿Qué pasa si el usuario necesita agregar piso y departamento?**
A: Después de seleccionar la dirección en Google Places, mostrar campos adicionales para piso y departamento.

**Q: ¿Los datos de Google Places se validan en el backend?**
A: El backend confía en los datos enviados. La validación principal es que la API Key sea válida en el frontend.

**Q: ¿Puedo limitar las búsquedas a una región específica?**
A: Sí, usa `componentRestrictions: { country: "ar" }` en las opciones del autocomplete.

