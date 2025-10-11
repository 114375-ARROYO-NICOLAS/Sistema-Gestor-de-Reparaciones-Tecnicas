# Refinamiento del Sistema de Gestión de Clientes

## Fecha: 08/10/2025

Este documento detalla las mejoras implementadas en el sistema de gestión de clientes del backend SIGRET.

## 1. Baja Lógica (Soft Delete)

### Cambios en la Entidad
- **Archivo**: `Cliente.java`
- **Cambio**: Se agregó el campo `activo` (Boolean) con valor por defecto `true`
- **Propósito**: Implementar baja lógica en lugar de eliminación física

### Migración de Base de Datos
- **Archivo**: `migracion_cliente_activo.sql` (proporcionado por si se necesita)

#### ⚠️ IMPORTANTE - No es necesario ejecutar el SQL manualmente

Como el proyecto tiene configurado `ddl-auto: update` en `application.yml`, **Hibernate aplicará automáticamente** el cambio al iniciar la aplicación:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ← Aplica cambios automáticamente
```

**El script SQL solo sería necesario si**:
- Usas `ddl-auto: validate` o `none` (típico en producción)
- Usas Flyway o Liquibase para migraciones controladas
- Necesitas ejecutar el cambio sin reiniciar la aplicación

## 2. Permisos Ajustados

### Antes
- **Crear/Actualizar**: Solo PROPIETARIO y ADMINISTRATIVO
- **Eliminar**: Solo PROPIETARIO
- **Ver/Buscar**: PROPIETARIO, ADMINISTRATIVO y TECNICO

### Ahora
- **Crear**: Cualquier empleado (PROPIETARIO, ADMINISTRATIVO, TECNICO)
- **Actualizar**: Cualquier empleado (PROPIETARIO, ADMINISTRATIVO, TECNICO)
- **Eliminar (baja lógica)**: PROPIETARIO y ADMINISTRATIVO
- **Reactivar**: PROPIETARIO y ADMINISTRATIVO
- **Ver/Buscar**: Cualquier empleado

### Justificación
Todos los empleados pueden necesitar crear o actualizar clientes en el proceso de atención. Solo roles administrativos pueden dar de baja o reactivar clientes.

## 3. Búsqueda Mejorada con Autocompletado

### Endpoint Nuevo: `/api/clientes/autocompletado`
- **Método**: GET
- **Parámetros**:
  - `termino` (String): Término de búsqueda
  - `limite` (int, default=10): Número máximo de resultados
- **Funcionalidad**: 
  - Búsqueda case-insensitive
  - Busca en: nombre, apellido, razón social y documento
  - Optimizado para autocompletado en tiempo real
  - Solo retorna clientes activos

### Query Optimizada
```java
@Query("SELECT c FROM Cliente c JOIN c.persona p " +
       "WHERE c.activo = true AND " +
       "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
       "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
       "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
       "p.documento LIKE CONCAT('%', :termino, '%'))")
List<Cliente> buscarClientesPorTermino(@Param("termino") String termino, Pageable pageable);
```

## 4. Paginación con Filtros

### Endpoint Mejorado: `/api/clientes`
- **Parámetros**:
  - `Pageable`: Paginación y ordenamiento (size, page, sort)
  - `filtro` (String, opcional): Filtro de búsqueda

### Ejemplos de Uso desde Frontend

#### Sin filtro (paginación básica)
```typescript
GET /api/clientes?page=0&size=10&sort=id,desc
```

#### Con filtro de búsqueda
```typescript
GET /api/clientes?page=0&size=10&filtro=juan
```

#### Con ordenamiento personalizado
```typescript
GET /api/clientes?page=0&size=10&sort=persona.nombre,asc&filtro=garcia
```

## 5. Endpoints Actualizados

### Eliminados
- ❌ `/api/clientes/verificar-documento` - La validación ahora es interna del servicio

### Modificados
- ✅ `GET /api/clientes` - Ahora acepta parámetro `filtro`
- ✅ `DELETE /api/clientes/{id}` - Ahora hace baja lógica (soft delete)
- ✅ Todos los endpoints GET - Solo retornan clientes activos

### Nuevos
- ✅ `GET /api/clientes/autocompletado` - Para autocompletado en tiempo real
- ✅ `PUT /api/clientes/{id}/reactivar` - Para reactivar clientes dados de baja

## 6. Validaciones Internas

### Validación de Documento Duplicado
- **Antes**: Endpoint público `/verificar-documento`
- **Ahora**: Validación interna en el servicio al crear cliente
- **Ventaja**: Mejor seguridad, menos exposición de lógica de negocio

### Código
```java
if (clienteRepository.existsByPersonaDocumento(clienteCreateDto.getDocumento())) {
    throw new DocumentoAlreadyExistsException("Ya existe un cliente con el documento: " + clienteCreateDto.getDocumento());
}
```

## 7. Creación en Cascada (Ya Implementado)

El sistema ya implementa correctamente la creación en cascada:
1. **Crear Persona**: Se crea primero la entidad Persona con todos sus datos
2. **Crear Cliente**: Se asocia el Cliente a la Persona
3. **Crear Direcciones**: Se crean las direcciones asociadas a la Persona

### Flujo en el Servicio
```java
// 1. Crear Persona
Persona personaGuardada = personaRepository.save(persona);

// 2. Crear Cliente
Cliente cliente = new Cliente(personaGuardada);
Cliente clienteGuardado = clienteRepository.save(cliente);

// 3. Crear Direcciones
if (clienteCreateDto.getDirecciones() != null) {
    crearDirecciones(personaGuardada, clienteCreateDto.getDirecciones());
}
```

## 8. Mejoras en Repositorio

### Nuevos Métodos
```java
// Solo clientes activos
Page<Cliente> findByActivoTrue(Pageable pageable);
List<Cliente> findByActivoTrue();

// Búsqueda por documento (solo activos)
Optional<Cliente> findByPersonaDocumentoAndActivoTrue(String documento);

// Búsqueda con filtros paginada
Page<Cliente> buscarClientesConFiltros(@Param("termino") String termino, Pageable pageable);

// Búsqueda para autocompletado
List<Cliente> buscarClientesPorTermino(@Param("termino") String termino, Pageable pageable);

// Buscar incluyendo inactivos (solo para operaciones administrativas)
Optional<Cliente> findByIdIncludingInactive(@Param("id") Long id);
```

## 9. Integración con Frontend

### Para el Componente de Gestión de Clientes

#### 1. Listado con Paginación (PrimeNG Table)
```typescript
loadClientes(event: LazyLoadEvent) {
    const page = event.first! / event.rows!;
    const size = event.rows!;
    const sort = event.sortField ? `${event.sortField},${event.sortOrder === 1 ? 'asc' : 'desc'}` : 'id,desc';
    const filtro = event.globalFilter || '';

    this.clienteService.getClientes(page, size, sort, filtro).subscribe(response => {
        this.clientes = response.content;
        this.totalRecords = response.totalElements;
    });
}
```

#### 2. Autocompletado (PrimeNG AutoComplete)
```typescript
buscarClientes(event: any) {
    this.clienteService.autocompletarClientes(event.query, 10).subscribe(clientes => {
        this.sugerenciasClientes = clientes;
    });
}
```

#### 3. Servicio Angular
```typescript
// En cliente.service.ts
getClientes(page: number, size: number, sort: string, filtro?: string): Observable<Page<ClienteListDto>> {
    let params = new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString())
        .set('sort', sort);
    
    if (filtro && filtro.trim()) {
        params = params.set('filtro', filtro);
    }
    
    return this.http.get<Page<ClienteListDto>>(`${this.apiUrl}/clientes`, { params });
}

autocompletarClientes(termino: string, limite: number = 10): Observable<ClienteListDto[]> {
    const params = new HttpParams()
        .set('termino', termino)
        .set('limite', limite.toString());
    
    return this.http.get<ClienteListDto[]>(`${this.apiUrl}/clientes/autocompletado`, { params });
}
```

### Recomendaciones Frontend

1. **Paginación y Filtros**: Usar PrimeNG Table con modo `lazy` para aprovechar la paginación del backend
2. **Autocompletado**: Usar PrimeNG AutoComplete para búsqueda de clientes en tiempo real
3. **Nunca buscar por ID**: Los usuarios siempre buscan por nombre o documento
4. **Ordenamiento**: Permitir ordenar por cualquier columna (nombre, documento, etc.)

## 10. Seguridad

### Búsqueda sin Exposición de IDs
- ❌ No se expone búsqueda por ID en el frontend
- ✅ Búsqueda por nombre, apellido, razón social y documento
- ✅ Los IDs solo se usan internamente después de seleccionar un cliente

### Validaciones
- Validación de documento duplicado es interna
- Solo clientes activos son visibles en búsquedas normales
- Permisos granulares según rol del empleado

## 11. Testing Recomendado

### Casos de Prueba
1. ✅ Crear cliente con persona y direcciones
2. ✅ Buscar cliente por nombre (case-insensitive)
3. ✅ Buscar cliente por documento
4. ✅ Paginación con filtros
5. ✅ Autocompletado con límite de resultados
6. ✅ Baja lógica de cliente
7. ✅ Reactivación de cliente
8. ✅ Intentar crear cliente con documento duplicado
9. ✅ Permisos según rol de empleado

## 12. Próximos Pasos

### Opcional - Mejoras Futuras
1. **Auditoría**: Agregar campos `fechaBaja` y `usuarioBaja` para trazabilidad
2. **Historial**: Implementar tabla de auditoría para cambios en clientes
3. **Búsqueda Avanzada**: Agregar filtros por fecha de creación, tipo de persona, etc.
4. **Exportación**: Endpoint para exportar clientes a Excel/PDF
5. **Bulk Operations**: Operaciones masivas (activar/desactivar múltiples)

## Resumen de Archivos Modificados

### Backend
- ✅ `entities/Cliente.java` - Agregado campo `activo`
- ✅ `repositories/ClienteRepository.java` - Nuevos query methods
- ✅ `services/ClienteService.java` - Nuevos métodos de servicio
- ✅ `services/impl/ClienteServiceImpl.java` - Implementación completa
- ✅ `controllers/cliente/ClienteController.java` - Endpoints actualizados
- ✅ `migracion_cliente_activo.sql` - Script de migración

### Frontend (Recomendaciones)
- 📋 Actualizar `cliente.service.ts` con nuevos métodos
- 📋 Implementar componente con PrimeNG Table (lazy loading)
- 📋 Implementar autocompletado con PrimeNG AutoComplete
- 📋 Ajustar búsquedas para usar filtros del backend

## ⚙️ Despliegue

### Desarrollo (Con ddl-auto: update)
✅ **Simplemente inicia la aplicación** 
- Hibernate detectará el nuevo campo `activo`
- Ejecutará automáticamente: `ALTER TABLE clientes ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE`
- No requiere intervención manual

### Producción (Recomendado ddl-auto: validate o none)
Si cambias a `validate` o `none` en producción (recomendado), ejecuta manualmente:
```sql
ALTER TABLE clientes ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
UPDATE clientes SET activo = TRUE WHERE activo IS NULL;
```

