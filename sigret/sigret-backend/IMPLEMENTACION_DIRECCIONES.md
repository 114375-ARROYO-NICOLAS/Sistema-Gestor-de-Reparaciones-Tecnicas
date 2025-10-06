# Implementación de Entidad Direcciones

## Resumen

Se ha implementado exitosamente la entidad `Direccion` para registrar domicilios de personas en el sistema. Una persona puede tener múltiples direcciones asociadas.

## Componentes Creados

### 1. Entidad (Entity)

**Archivo:** `src/main/java/com/sigret/entities/Direccion.java`

**Campos:**
- `id`: Identificador único
- `persona`: Relación ManyToOne con Persona
- `calle`: Nombre de la calle (obligatorio)
- `numero`: Número de la dirección (obligatorio)
- `piso`: Número de piso (opcional)
- `departamento`: Número de departamento (opcional)
- `barrio`: Nombre del barrio (opcional)
- `ciudad`: Ciudad (obligatorio)
- `provincia`: Provincia/Estado (obligatorio)
- `codigoPostal`: Código postal (opcional)
- `pais`: País (obligatorio)
- `observaciones`: Observaciones adicionales (opcional)
- `esPrincipal`: Indica si es la dirección principal (boolean)

**Método Utilitario:**
- `getDireccionCompleta()`: Genera una representación textual completa de la dirección

### 2. Modificación en Persona

**Archivo:** `src/main/java/com/sigret/entities/Persona.java`

Se agregó la relación OneToMany:
```java
@OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Direccion> direcciones = new ArrayList<>();
```

### 3. Repositorio

**Archivo:** `src/main/java/com/sigret/repositories/DireccionRepository.java`

**Métodos disponibles:**
- `findByPersonaId(Long personaId)`: Busca todas las direcciones de una persona
- `findByPersonaIdAndEsPrincipalTrue(Long personaId)`: Busca la dirección principal
- `existsByPersonaId(Long personaId)`: Verifica si una persona tiene direcciones
- `countByPersonaId(Long personaId)`: Cuenta las direcciones de una persona
- `findByCiudadContainingIgnoreCase(String ciudad)`: Busca por ciudad
- `findByProvinciaContainingIgnoreCase(String provincia)`: Busca por provincia

### 4. DTOs (Data Transfer Objects)

**Directorio:** `src/main/java/com/sigret/dtos/direccion/`

**Archivos creados:**
- `DireccionCreateDto.java`: Para crear direcciones (incluye personaId)
- `DireccionUpdateDto.java`: Para actualizar direcciones
- `DireccionResponseDto.java`: Respuesta completa con todos los datos
- `DireccionListDto.java`: Respuesta simplificada para listados

### 5. Servicio

**Archivos:**
- `src/main/java/com/sigret/services/DireccionService.java` (Interface)
- `src/main/java/com/sigret/services/impl/DireccionServiceImpl.java` (Implementación)

**Métodos del servicio:**
- `crearDireccion()`: Crea una nueva dirección
- `obtenerDireccionPorId()`: Obtiene una dirección por ID
- `obtenerDireccionesPorPersona()`: Obtiene todas las direcciones de una persona
- `obtenerDireccionPrincipalPorPersona()`: Obtiene la dirección principal de una persona
- `obtenerDirecciones()`: Lista paginada de direcciones
- `buscarPorCiudad()`: Busca direcciones por ciudad
- `buscarPorProvincia()`: Busca direcciones por provincia
- `actualizarDireccion()`: Actualiza una dirección
- `eliminarDireccion()`: Elimina una dirección
- `marcarComoPrincipal()`: Marca una dirección como principal

**Lógica especial:**
- Al marcar una dirección como principal, automáticamente desmarca las otras direcciones de la misma persona
- Validación de existencia de persona al crear direcciones

### 6. Controlador REST

**Archivo:** `src/main/java/com/sigret/controllers/direccion/DireccionController.java`

**Base URL:** `/api/direcciones`

**Endpoints disponibles:**

| Método | Endpoint | Descripción | Roles Autorizados |
|--------|----------|-------------|-------------------|
| POST | `/api/direcciones` | Crear dirección | PROPIETARIO, ADMINISTRATIVO |
| GET | `/api/direcciones/{id}` | Obtener por ID | PROPIETARIO, ADMINISTRATIVO, TECNICO |
| GET | `/api/direcciones/persona/{personaId}` | Listar por persona | PROPIETARIO, ADMINISTRATIVO, TECNICO |
| GET | `/api/direcciones/persona/{personaId}/principal` | Obtener principal | PROPIETARIO, ADMINISTRATIVO, TECNICO |
| GET | `/api/direcciones` | Listar paginado | PROPIETARIO, ADMINISTRATIVO |
| GET | `/api/direcciones/buscar/ciudad?ciudad={ciudad}` | Buscar por ciudad | PROPIETARIO, ADMINISTRATIVO |
| GET | `/api/direcciones/buscar/provincia?provincia={provincia}` | Buscar por provincia | PROPIETARIO, ADMINISTRATIVO |
| PUT | `/api/direcciones/{id}` | Actualizar dirección | PROPIETARIO, ADMINISTRATIVO |
| PATCH | `/api/direcciones/{id}/marcar-principal` | Marcar como principal | PROPIETARIO, ADMINISTRATIVO |
| DELETE | `/api/direcciones/{id}` | Eliminar dirección | PROPIETARIO, ADMINISTRATIVO |

### 7. Excepción

**Archivo:** `src/main/java/com/sigret/exception/DireccionNotFoundException.java`

Excepción personalizada para cuando no se encuentra una dirección.

## Características Implementadas

### 1. Direcciones Múltiples
- Una persona puede tener múltiples direcciones asociadas
- Relación OneToMany desde Persona hacia Direccion

### 2. Dirección Principal
- Sistema para marcar una dirección como principal
- Solo puede haber una dirección principal por persona
- Al marcar una como principal, automáticamente se desmarcan las demás

### 3. Validaciones
- Campos obligatorios: calle, número, ciudad, provincia, país
- Validación de longitud máxima en todos los campos
- Verificación de existencia de persona al crear direcciones

### 4. Búsquedas Flexibles
- Búsqueda por persona
- Búsqueda por ciudad (case-insensitive)
- Búsqueda por provincia (case-insensitive)
- Listado paginado general

### 5. Seguridad
- Todos los endpoints requieren autenticación JWT (Bearer Token)
- Roles diferenciados según la operación
- Documentación Swagger/OpenAPI integrada

## Ejemplo de Uso

### Crear una Dirección

**Request:**
```json
POST /api/direcciones
{
  "personaId": 1,
  "calle": "Av. Libertador",
  "numero": "1234",
  "piso": "5",
  "departamento": "B",
  "barrio": "Centro",
  "ciudad": "Buenos Aires",
  "provincia": "Buenos Aires",
  "codigoPostal": "1001",
  "pais": "Argentina",
  "observaciones": "Edificio color azul",
  "esPrincipal": true
}
```

**Response:**
```json
{
  "id": 1,
  "personaId": 1,
  "nombrePersona": "Juan Pérez",
  "calle": "Av. Libertador",
  "numero": "1234",
  "piso": "5",
  "departamento": "B",
  "barrio": "Centro",
  "ciudad": "Buenos Aires",
  "provincia": "Buenos Aires",
  "codigoPostal": "1001",
  "pais": "Argentina",
  "observaciones": "Edificio color azul",
  "esPrincipal": true,
  "direccionCompleta": "Av. Libertador 1234, Piso 5, Depto. B, Centro, Buenos Aires, Buenos Aires (1001), Argentina"
}
```

### Obtener Direcciones de una Persona

**Request:**
```
GET /api/direcciones/persona/1
```

**Response:**
```json
[
  {
    "id": 1,
    "calle": "Av. Libertador",
    "numero": "1234",
    "ciudad": "Buenos Aires",
    "provincia": "Buenos Aires",
    "pais": "Argentina",
    "esPrincipal": true,
    "direccionCompleta": "Av. Libertador 1234, Piso 5, Depto. B, Centro, Buenos Aires, Buenos Aires (1001), Argentina"
  }
]
```

## Base de Datos

La tabla `direcciones` se creará automáticamente por JPA con la siguiente estructura:

```sql
CREATE TABLE direcciones (
    id_direccion BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_persona BIGINT NOT NULL,
    calle VARCHAR(200) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    piso VARCHAR(10),
    departamento VARCHAR(10),
    barrio VARCHAR(200),
    ciudad VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    pais VARCHAR(100) NOT NULL,
    observaciones VARCHAR(500),
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_persona) REFERENCES personas(id_persona)
);
```

## Documentación Swagger

La documentación de los endpoints está disponible en:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Los endpoints están agrupados bajo la etiqueta **"Gestión de Direcciones"**.

## Próximos Pasos Recomendados

1. **Migración de Base de Datos:** Si usas Flyway o Liquibase, crear el script de migración
2. **Tests Unitarios:** Implementar tests para DireccionService
3. **Tests de Integración:** Implementar tests para DireccionController
4. **Validaciones Adicionales:** 
   - Validación de formato de código postal por país
   - Geocodificación de direcciones (opcional)
5. **Auditoría:** Agregar campos de auditoría (createdAt, updatedAt, createdBy, updatedBy)

## Compilación

El proyecto compila exitosamente sin errores:
```bash
./mvnw clean compile
[INFO] BUILD SUCCESS
```

## Notas Técnicas

- **JPA/Hibernate:** Manejo automático de relaciones
- **Lazy Loading:** Las direcciones se cargan bajo demanda
- **Cascade:** Las direcciones se eliminan automáticamente si se elimina la persona
- **Transacciones:** Todas las operaciones de escritura son transaccionales
- **Validación Bean:** Validaciones declarativas con Jakarta Validation

