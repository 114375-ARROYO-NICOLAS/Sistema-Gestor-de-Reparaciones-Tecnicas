# Mejora de Listado de Clientes - Dirección Principal

## Fecha: 08/10/2025

## Objetivo

Agregar la dirección principal formateada en el listado de clientes para mostrarla en la tabla de gestión.

## Cambios Implementados

### 1. ClienteListDto.java

**Agregado campo:**
```java
private String direccionPrincipal;
```

**DTO completo:**
```java
public class ClienteListDto {
    private Long id;
    private String nombreCompleto;
    private String documento;
    private String email;
    private String telefono;
    private String direccionPrincipal;  // ← Nuevo campo
    private Boolean esPersonaJuridica;
}
```

### 2. ClienteServiceImpl.java

#### Método de Conversión Actualizado

```java
private ClienteListDto convertirAClienteListDto(Cliente cliente) {
    // Obtener dirección principal formateada
    String direccionPrincipal = obtenerDireccionPrincipalFormateada(cliente.getPersona().getId());
    
    return new ClienteListDto(
            cliente.getId(),
            cliente.getNombreCompleto(),
            cliente.getDocumento(),
            cliente.getPrimerEmail(),
            cliente.getPrimerTelefono(),
            direccionPrincipal,  // ← Agregado
            cliente.esPersonaJuridica()
    );
}
```

#### Métodos Auxiliares Agregados

**1. obtenerDireccionPrincipalFormateada()**
```java
/**
 * Obtiene la dirección principal de una persona y la formatea de forma corta
 * Formato: "calle numero, barrio" o "calle numero - piso departamento, barrio"
 */
private String obtenerDireccionPrincipalFormateada(Long personaId) {
    List<Direccion> direcciones = direccionRepository.findByPersonaId(personaId);
    
    if (direcciones == null || direcciones.isEmpty()) {
        return null;
    }
    
    // Buscar dirección principal o la primera si no hay principal
    Direccion direccionPrincipal = direcciones.stream()
            .filter(Direccion::getEsPrincipal)
            .findFirst()
            .orElse(direcciones.get(0));
    
    return formatearDireccionCorta(direccionPrincipal);
}
```

**2. formatearDireccionCorta()**
```java
/**
 * Formatea una dirección de forma corta
 * Formato: "calle numero, barrio" o "calle numero - piso departamento, barrio"
 */
private String formatearDireccionCorta(Direccion direccion) {
    if (direccion == null) {
        return null;
    }
    
    StringBuilder direccionCorta = new StringBuilder();
    
    // Calle y número
    if (direccion.getCalle() != null && !direccion.getCalle().trim().isEmpty()) {
        direccionCorta.append(direccion.getCalle());
        
        if (direccion.getNumero() != null && !direccion.getNumero().trim().isEmpty()) {
            direccionCorta.append(" ").append(direccion.getNumero());
        }
    }
    
    // Piso y departamento (si existen)
    if ((direccion.getPiso() != null && !direccion.getPiso().trim().isEmpty()) ||
        (direccion.getDepartamento() != null && !direccion.getDepartamento().trim().isEmpty())) {
        
        direccionCorta.append(" - ");
        
        if (direccion.getPiso() != null && !direccion.getPiso().trim().isEmpty()) {
            direccionCorta.append(direccion.getPiso());
        }
        
        if (direccion.getDepartamento() != null && !direccion.getDepartamento().trim().isEmpty()) {
            if (direccion.getPiso() != null && !direccion.getPiso().trim().isEmpty()) {
                direccionCorta.append(" ");
            }
            direccionCorta.append(direccion.getDepartamento());
        }
    }
    
    // Barrio
    if (direccion.getBarrio() != null && !direccion.getBarrio().trim().isEmpty()) {
        if (direccionCorta.length() > 0) {
            direccionCorta.append(", ");
        }
        direccionCorta.append(direccion.getBarrio());
    }
    
    return direccionCorta.length() > 0 ? direccionCorta.toString() : null;
}
```

## Ejemplos de Formato

### Caso 1: Solo calle, número y barrio
**Datos:**
- Calle: "Av. Siempre Viva"
- Número: "742"
- Barrio: "Centro"

**Resultado:** `"Av. Siempre Viva 742, Centro"`

### Caso 2: Con piso y departamento
**Datos:**
- Calle: "San Martín"
- Número: "123"
- Piso: "5"
- Departamento: "A"
- Barrio: "Recoleta"

**Resultado:** `"San Martín 123 - 5 A, Recoleta"`

### Caso 3: Sin barrio
**Datos:**
- Calle: "Rivadavia"
- Número: "456"
- Piso: "2"
- Departamento: "B"

**Resultado:** `"Rivadavia 456 - 2 B"`

### Caso 4: Sin dirección
**Datos:** Cliente sin direcciones

**Resultado:** `null`

## Lógica de Selección de Dirección

1. **Busca la dirección marcada como principal** (`esPrincipal = true`)
2. Si no hay dirección principal, **usa la primera dirección** de la lista
3. Si no hay direcciones, retorna `null`

## Respuesta del API

### GET /api/clientes?page=0&size=10

```json
{
  "content": [
    {
      "id": 1,
      "nombreCompleto": "Juan Pérez",
      "documento": "12345678",
      "email": "juan@email.com",
      "telefono": "+54 9 11 1234-5678",
      "direccionPrincipal": "Av. Siempre Viva 742, Centro",
      "esPersonaJuridica": false
    },
    {
      "id": 2,
      "nombreCompleto": "María González",
      "documento": "87654321",
      "email": "maria@empresa.com",
      "telefono": "+54 9 11 8765-4321",
      "direccionPrincipal": "San Martín 123 - 5 A, Recoleta",
      "esPersonaJuridica": false
    },
    {
      "id": 3,
      "nombreCompleto": "Empresa SA",
      "documento": "30123456789",
      "email": "contacto@empresa.com",
      "telefono": "+54 11 4444-5555",
      "direccionPrincipal": null,
      "esPersonaJuridica": true
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

## Integración con Frontend (PrimeNG Table)

### TypeScript Interface

```typescript
export interface ClienteListDto {
  id: number;
  nombreCompleto: string;
  documento: string;
  email: string;
  telefono: string;
  direccionPrincipal: string;  // ← Nuevo campo
  esPersonaJuridica: boolean;
}
```

### Template HTML (PrimeNG Table)

```html
<p-table 
  [value]="clientes"
  [lazy]="true"
  (onLazyLoad)="loadClientes($event)"
  [paginator]="true"
  [rows]="10"
  [totalRecords]="totalRecords">
  
  <ng-template pTemplate="header">
    <tr>
      <th pSortableColumn="nombreCompleto">Nombre <p-sortIcon field="nombreCompleto"></p-sortIcon></th>
      <th pSortableColumn="documento">Documento <p-sortIcon field="documento"></p-sortIcon></th>
      <th>Email</th>
      <th>Teléfono</th>
      <th>Dirección</th>
      <th>Acciones</th>
    </tr>
  </ng-template>
  
  <ng-template pTemplate="body" let-cliente>
    <tr>
      <td>{{ cliente.nombreCompleto }}</td>
      <td>{{ cliente.documento }}</td>
      <td>{{ cliente.email || '-' }}</td>
      <td>{{ cliente.telefono || '-' }}</td>
      <td>
        <span *ngIf="cliente.direccionPrincipal" 
              class="direccion-corta"
              [pTooltip]="cliente.direccionPrincipal">
          {{ cliente.direccionPrincipal }}
        </span>
        <span *ngIf="!cliente.direccionPrincipal" class="text-muted">
          Sin dirección
        </span>
      </td>
      <td>
        <button pButton icon="pi pi-eye" 
                class="p-button-rounded p-button-info p-button-sm"
                (click)="verDetalles(cliente)"></button>
        <button pButton icon="pi pi-pencil" 
                class="p-button-rounded p-button-warning p-button-sm"
                (click)="editar(cliente)"></button>
      </td>
    </tr>
  </ng-template>
</p-table>
```

### CSS Recomendado

```scss
.direccion-corta {
  display: inline-block;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  &:hover {
    cursor: help;
  }
}
```

## Ventajas

1. ✅ **Performance**: La dirección se formatea una sola vez en el backend
2. ✅ **Consistencia**: Formato uniforme en todo el sistema
3. ✅ **UX mejorada**: Vista rápida de la ubicación del cliente
4. ✅ **Sin impacto**: Si no hay dirección, muestra `null` (manejable en frontend)

## Testing Recomendado

### Casos de Prueba
1. ✅ Cliente con dirección principal (calle + número + barrio)
2. ✅ Cliente con dirección principal (calle + número + piso + depto + barrio)
3. ✅ Cliente con múltiples direcciones (debe usar la principal)
4. ✅ Cliente sin dirección principal pero con direcciones (usa la primera)
5. ✅ Cliente sin direcciones (retorna null)
6. ✅ Dirección con campos vacíos o null

## Notas Importantes

- Si el cliente tiene múltiples direcciones, se mostrará solo la marcada como `esPrincipal = true`
- Si ninguna está marcada como principal, se mostrará la primera de la lista
- El campo puede ser `null` si el cliente no tiene direcciones
- El formato es compacto para caber en una columna de tabla

## Archivos Modificados

- ✅ `dtos/cliente/ClienteListDto.java` - Agregado campo direccionPrincipal
- ✅ `services/impl/ClienteServiceImpl.java` - Agregados métodos de formateo

