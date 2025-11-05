# 🚀 INSTRUCCIONES PARA COMPLETAR LA IMPLEMENTACIÓN DEL BACKEND

## ✅ COMPLETADO:
1. ✅ `PresupuestoEventDto.java` - Creado
2. ✅ `OrdenTrabajoEventDto.java` - Creado
3. ✅ `PresupuestoListDto.java` - Actualizado con `empleadoNombre` y `equipoDescripcion`
4. ✅ `WebSocketConfig.java` - Actualizado con nuevos topics
5. ✅ `WebSocketNotificationService.java` - Agregados métodos para presupuestos y órdenes

---

## 📋 PENDIENTE DE IMPLEMENTAR:

### PASO 5: Modificar `ServicioServiceImpl.java`

**Archivo:** `src/main/java/com/sigret/services/impl/ServicioServiceImpl.java`

**En el método `crearServicio()`**, después de guardar el servicio y ANTES del return, agregar:

```java
// Auto-crear presupuesto pendiente
Presupuesto presupuesto = new Presupuesto();
presupuesto.setServicio(servicioGuardado);
presupuesto.setEstado(EstadoPresupuesto.PENDIENTE);
presupuesto.setFechaCreacion(LocalDateTime.now());
presupuesto.setFechaSolicitud(LocalDate.now());
presupuesto.setMostrarOriginal(true);
presupuesto.setMostrarAlternativo(false);
// No asignar empleado todavía (quedará null), se asignará cuando alguien lo tome
presupuestoRepository.save(presupuesto);

// Notificar por WebSocket
PresupuestoEventDto presupuestoEvent = new PresupuestoEventDto();
presupuestoEvent.setTipoEvento("CREADO");
presupuestoEvent.setPresupuestoId(presupuesto.getId());
presupuestoEvent.setServicioId(servicioGuardado.getId());
presupuestoEvent.setNumeroServicio(servicioGuardado.getNumeroServicio());
presupuestoEvent.setEstadoNuevo(EstadoPresupuesto.PENDIENTE);
webSocketNotificationService.notificarPresupuesto(presupuestoEvent);
```

**Imports necesarios:**
```java
import com.sigret.entities.Presupuesto;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.dtos.presupuesto.PresupuestoEventDto;
```

**Inyectar dependencia:**
```java
@Autowired
private PresupuestoRepository presupuestoRepository;
```

---

### PASO 6: Actualizar `PresupuestoController.java`

**Archivo:** `src/main/java/com/sigret/controllers/presupuesto/PresupuestoController.java`

**Agregar estos 3 endpoints:**

```java
@PatchMapping("/{id}/asignar-empleado")
@Operation(summary = "Asignar empleado a presupuesto", description = "Asigna un empleado para que realice el presupuesto")
@PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
public ResponseEntity<PresupuestoResponseDto> asignarEmpleado(
        @Parameter(description = "ID del presupuesto") @PathVariable Long id,
        @Parameter(description = "ID del empleado") @RequestParam Long empleadoId) {
    PresupuestoResponseDto presupuesto = presupuestoService.asignarEmpleado(id, empleadoId);
    return ResponseEntity.ok(presupuesto);
}

@PatchMapping("/{id}/aprobar")
@Operation(summary = "Aprobar presupuesto", description = "Aprueba un presupuesto y crea automáticamente la orden de trabajo")
@PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
public ResponseEntity<PresupuestoResponseDto> aprobarPresupuesto(
        @Parameter(description = "ID del presupuesto") @PathVariable Long id,
        @Parameter(description = "Tipo confirmado (ORIGINAL o ALTERNATIVO)") @RequestParam(required = false) String tipoConfirmado,
        @Parameter(description = "Canal de confirmación") @RequestParam(required = false) String canalConfirmacion) {
    PresupuestoResponseDto presupuesto = presupuestoService.aprobarPresupuesto(id, tipoConfirmado, canalConfirmacion);
    return ResponseEntity.ok(presupuesto);
}

@PatchMapping("/{id}/rechazar")
@Operation(summary = "Rechazar presupuesto", description = "Rechaza un presupuesto")
@PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
public ResponseEntity<PresupuestoResponseDto> rechazarPresupuesto(
        @Parameter(description = "ID del presupuesto") @PathVariable Long id,
        @Parameter(description = "Motivo del rechazo") @RequestParam(required = false) String motivo) {
    PresupuestoResponseDto presupuesto = presupuestoService.rechazarPresupuesto(id, motivo);
    return ResponseEntity.ok(presupuesto);
}
```

---

### PASO 7: Actualizar `PresupuestoService.java` (interface)

**Archivo:** `src/main/java/com/sigret/services/PresupuestoService.java`

**Agregar estas firmas de métodos:**

```java
PresupuestoResponseDto asignarEmpleado(Long presupuestoId, Long empleadoId);
PresupuestoResponseDto aprobarPresupuesto(Long presupuestoId, String tipoConfirmado, String canalConfirmacion);
PresupuestoResponseDto rechazarPresupuesto(Long presupuestoId, String motivo);
```

---

### PASO 8: Implementar en `PresupuestoServiceImpl.java`

**Archivo:** `src/main/java/com/sigret/services/impl/PresupuestoServiceImpl.java`

**Imports adicionales necesarios:**
```java
import com.sigret.entities.OrdenTrabajo;
import com.sigret.repositories.OrdenTrabajoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoConfirmacion;
import com.sigret.enums.CanalConfirmacion;
import com.sigret.dtos.presupuesto.PresupuestoEventDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoEventDto;
import com.sigret.dtos.servicio.ServicioEventDto;
import com.sigret.services.WebSocketNotificationService;
```

**Inyectar dependencias:**
```java
@Autowired
private OrdenTrabajoRepository ordenTrabajoRepository;

@Autowired
private ServicioRepository servicioRepository;

@Autowired
private EmpleadoRepository empleadoRepository;

@Autowired
private WebSocketNotificationService webSocketNotificationService;
```

**Implementar método `asignarEmpleado()`:**

```java
@Override
@Transactional
public PresupuestoResponseDto asignarEmpleado(Long presupuestoId, Long empleadoId) {
    Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
            .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));

    Empleado empleado = empleadoRepository.findById(empleadoId)
            .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + empleadoId));

    presupuesto.setEmpleado(empleado);
    Presupuesto presupuestoGuardado = presupuestoRepository.save(presupuesto);

    // Notificar WebSocket
    PresupuestoEventDto evento = new PresupuestoEventDto();
    evento.setTipoEvento("ACTUALIZADO");
    evento.setPresupuestoId(presupuestoGuardado.getId());
    evento.setServicioId(presupuestoGuardado.getServicio().getId());
    evento.setNumeroServicio(presupuestoGuardado.getServicio().getNumeroServicio());
    webSocketNotificationService.notificarPresupuesto(evento);

    return presupuestoMapper.toResponseDto(presupuestoGuardado);
}
```

**Implementar método `aprobarPresupuesto()` - CLAVE:**

```java
@Override
@Transactional
public PresupuestoResponseDto aprobarPresupuesto(Long presupuestoId, String tipoConfirmado, String canalConfirmacion) {
    Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
            .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));

    EstadoPresupuesto estadoAnterior = presupuesto.getEstado();

    // 1. Actualizar presupuesto a APROBADO
    presupuesto.setEstado(EstadoPresupuesto.APROBADO);
    presupuesto.setFechaConfirmacion(LocalDateTime.now());
    if (tipoConfirmado != null) {
        presupuesto.setTipoConfirmado(TipoConfirmacion.valueOf(tipoConfirmado));
    }
    if (canalConfirmacion != null) {
        presupuesto.setCanalConfirmacion(CanalConfirmacion.valueOf(canalConfirmacion));
    }
    Presupuesto presupuestoGuardado = presupuestoRepository.save(presupuesto);

    // 2. Cambiar estado del SERVICIO a APROBADO
    Servicio servicio = presupuesto.getServicio();
    servicio.setEstado(EstadoServicio.APROBADO);
    servicioRepository.save(servicio);

    // 3. Crear ORDEN DE TRABAJO automáticamente
    OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
    ordenTrabajo.setServicio(servicio);
    ordenTrabajo.setPresupuesto(presupuestoGuardado);
    ordenTrabajo.setEstado(EstadoOrdenTrabajo.PENDIENTE);
    ordenTrabajo.setFechaCreacion(LocalDateTime.now());
    ordenTrabajo.setEsSinCosto(servicio.getEsGarantia());
    ordenTrabajo.setMontoTotalRepuestos(BigDecimal.ZERO);
    ordenTrabajo.setMontoExtras(BigDecimal.ZERO);
    // No asignar empleado todavía, se asignará cuando alguien la tome
    OrdenTrabajo ordenGuardada = ordenTrabajoRepository.save(ordenTrabajo);

    // 4. Notificar cambios por WebSocket
    // Notificar presupuesto
    PresupuestoEventDto presupuestoEvent = new PresupuestoEventDto();
    presupuestoEvent.setTipoEvento("CAMBIO_ESTADO");
    presupuestoEvent.setPresupuestoId(presupuestoGuardado.getId());
    presupuestoEvent.setServicioId(servicio.getId());
    presupuestoEvent.setNumeroServicio(servicio.getNumeroServicio());
    presupuestoEvent.setEstadoAnterior(estadoAnterior);
    presupuestoEvent.setEstadoNuevo(EstadoPresupuesto.APROBADO);
    webSocketNotificationService.notificarPresupuesto(presupuestoEvent);

    // Notificar servicio
    ServicioEventDto servicioEvent = new ServicioEventDto();
    servicioEvent.setTipo("CAMBIO_ESTADO");
    servicioEvent.setServicioId(servicio.getId());
    servicioEvent.setNumeroServicio(servicio.getNumeroServicio());
    webSocketNotificationService.notificarServicioActualizado(servicioMapper.toListDto(servicio));

    // Notificar orden de trabajo
    OrdenTrabajoEventDto ordenEvent = new OrdenTrabajoEventDto();
    ordenEvent.setTipoEvento("CREADO");
    ordenEvent.setOrdenTrabajoId(ordenGuardada.getId());
    ordenEvent.setServicioId(servicio.getId());
    ordenEvent.setNumeroServicio(servicio.getNumeroServicio());
    ordenEvent.setEstadoNuevo(EstadoOrdenTrabajo.PENDIENTE);
    webSocketNotificationService.notificarOrdenTrabajo(ordenEvent);

    return presupuestoMapper.toResponseDto(presupuestoGuardado);
}
```

**Implementar método `rechazarPresupuesto()`:**

```java
@Override
@Transactional
public PresupuestoResponseDto rechazarPresupuesto(Long presupuestoId, String motivo) {
    Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
            .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));

    EstadoPresupuesto estadoAnterior = presupuesto.getEstado();

    // Actualizar presupuesto a RECHAZADO
    presupuesto.setEstado(EstadoPresupuesto.RECHAZADO);
    presupuesto.setFechaConfirmacion(LocalDateTime.now());
    Presupuesto presupuestoGuardado = presupuestoRepository.save(presupuesto);

    // Cambiar estado del SERVICIO a RECHAZADO
    Servicio servicio = presupuesto.getServicio();
    servicio.setEstado(EstadoServicio.RECHAZADO);
    servicioRepository.save(servicio);

    // Notificar por WebSocket
    PresupuestoEventDto presupuestoEvent = new PresupuestoEventDto();
    presupuestoEvent.setTipoEvento("CAMBIO_ESTADO");
    presupuestoEvent.setPresupuestoId(presupuestoGuardado.getId());
    presupuestoEvent.setServicioId(servicio.getId());
    presupuestoEvent.setNumeroServicio(servicio.getNumeroServicio());
    presupuestoEvent.setEstadoAnterior(estadoAnterior);
    presupuestoEvent.setEstadoNuevo(EstadoPresupuesto.RECHAZADO);
    webSocketNotificationService.notificarPresupuesto(presupuestoEvent);

    ServicioEventDto servicioEvent = new ServicioEventDto();
    servicioEvent.setTipo("CAMBIO_ESTADO");
    servicioEvent.setServicioId(servicio.getId());
    servicioEvent.setNumeroServicio(servicio.getNumeroServicio());
    webSocketNotificationService.notificarServicioActualizado(servicioMapper.toListDto(servicio));

    return presupuestoMapper.toResponseDto(presupuestoGuardado);
}
```

---

### PASO 9: Actualizar `OrdenTrabajoController.java`

**Archivo:** `src/main/java/com/sigret/controllers/ordenTrabajo/OrdenTrabajoController.java`

**Agregar estos 3 endpoints:**

```java
@PatchMapping("/{id}/asignar-empleado")
@Operation(summary = "Asignar empleado a orden de trabajo", description = "Asigna un empleado para que realice la orden de trabajo")
@PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
public ResponseEntity<OrdenTrabajoResponseDto> asignarEmpleado(
        @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id,
        @Parameter(description = "ID del empleado") @RequestParam Long empleadoId) {
    OrdenTrabajoResponseDto orden = ordenTrabajoService.asignarEmpleado(id, empleadoId);
    return ResponseEntity.ok(orden);
}

@PatchMapping("/{id}/iniciar")
@Operation(summary = "Iniciar orden de trabajo", description = "Cambia el estado a EN_PROGRESO y actualiza el servicio a EN_REPARACION")
@PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
public ResponseEntity<OrdenTrabajoResponseDto> iniciarOrden(
        @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id) {
    OrdenTrabajoResponseDto orden = ordenTrabajoService.iniciarOrden(id);
    return ResponseEntity.ok(orden);
}

@PatchMapping("/{id}/terminar")
@Operation(summary = "Terminar orden de trabajo", description = "Cambia el estado a TERMINADA y actualiza el servicio a TERMINADO")
@PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
public ResponseEntity<OrdenTrabajoResponseDto> terminarOrden(
        @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id) {
    OrdenTrabajoResponseDto orden = ordenTrabajoService.terminarOrden(id);
    return ResponseEntity.ok(orden);
}
```

---

### PASO 10: Actualizar `OrdenTrabajoService.java` (interface)

**Archivo:** `src/main/java/com/sigret/services/OrdenTrabajoService.java`

**Agregar estas firmas de métodos:**

```java
OrdenTrabajoResponseDto asignarEmpleado(Long ordenId, Long empleadoId);
OrdenTrabajoResponseDto iniciarOrden(Long ordenId);
OrdenTrabajoResponseDto terminarOrden(Long ordenId);
```

---

### PASO 11: Implementar en `OrdenTrabajoServiceImpl.java`

**Archivo:** `src/main/java/com/sigret/services/impl/OrdenTrabajoServiceImpl.java`

**Imports adicionales necesarios:**
```java
import com.sigret.repositories.ServicioRepository;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.enums.EstadoServicio;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoEventDto;
import com.sigret.dtos.servicio.ServicioEventDto;
import com.sigret.services.WebSocketNotificationService;
```

**Inyectar dependencias:**
```java
@Autowired
private ServicioRepository servicioRepository;

@Autowired
private EmpleadoRepository empleadoRepository;

@Autowired
private WebSocketNotificationService webSocketNotificationService;
```

**Implementar método `asignarEmpleado()`:**

```java
@Override
@Transactional
public OrdenTrabajoResponseDto asignarEmpleado(Long ordenId, Long empleadoId) {
    OrdenTrabajo orden = ordenTrabajoRepository.findById(ordenId)
            .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + ordenId));

    Empleado empleado = empleadoRepository.findById(empleadoId)
            .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + empleadoId));

    orden.setEmpleado(empleado);
    OrdenTrabajo ordenGuardada = ordenTrabajoRepository.save(orden);

    // Notificar WebSocket
    OrdenTrabajoEventDto evento = new OrdenTrabajoEventDto();
    evento.setTipoEvento("ACTUALIZADO");
    evento.setOrdenTrabajoId(ordenGuardada.getId());
    evento.setServicioId(ordenGuardada.getServicio().getId());
    evento.setNumeroServicio(ordenGuardada.getServicio().getNumeroServicio());
    webSocketNotificationService.notificarOrdenTrabajo(evento);

    return ordenTrabajoMapper.toResponseDto(ordenGuardada);
}
```

**Implementar método `iniciarOrden()`:**

```java
@Override
@Transactional
public OrdenTrabajoResponseDto iniciarOrden(Long ordenId) {
    OrdenTrabajo orden = ordenTrabajoRepository.findById(ordenId)
            .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + ordenId));

    EstadoOrdenTrabajo estadoAnterior = orden.getEstado();

    orden.setEstado(EstadoOrdenTrabajo.EN_PROGRESO);
    orden.setFechaComienzo(LocalDate.now());
    OrdenTrabajo ordenGuardada = ordenTrabajoRepository.save(orden);

    // Cambiar estado del SERVICIO a EN_REPARACION
    Servicio servicio = orden.getServicio();
    servicio.setEstado(EstadoServicio.EN_REPARACION);
    servicioRepository.save(servicio);

    // Notificar cambios por WebSocket
    OrdenTrabajoEventDto ordenEvent = new OrdenTrabajoEventDto();
    ordenEvent.setTipoEvento("CAMBIO_ESTADO");
    ordenEvent.setOrdenTrabajoId(ordenGuardada.getId());
    ordenEvent.setServicioId(servicio.getId());
    ordenEvent.setNumeroServicio(servicio.getNumeroServicio());
    ordenEvent.setEstadoAnterior(estadoAnterior);
    ordenEvent.setEstadoNuevo(EstadoOrdenTrabajo.EN_PROGRESO);
    webSocketNotificationService.notificarOrdenTrabajo(ordenEvent);

    ServicioEventDto servicioEvent = new ServicioEventDto();
    servicioEvent.setTipo("CAMBIO_ESTADO");
    servicioEvent.setServicioId(servicio.getId());
    servicioEvent.setNumeroServicio(servicio.getNumeroServicio());
    webSocketNotificationService.notificarServicioActualizado(servicioMapper.toListDto(servicio));

    return ordenTrabajoMapper.toResponseDto(ordenGuardada);
}
```

**Implementar método `terminarOrden()` - CLAVE:**

```java
@Override
@Transactional
public OrdenTrabajoResponseDto terminarOrden(Long ordenId) {
    OrdenTrabajo orden = ordenTrabajoRepository.findById(ordenId)
            .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + ordenId));

    EstadoOrdenTrabajo estadoAnterior = orden.getEstado();

    orden.setEstado(EstadoOrdenTrabajo.TERMINADA);
    orden.setFechaFin(LocalDate.now());
    OrdenTrabajo ordenGuardada = ordenTrabajoRepository.save(orden);

    // Cambiar estado del SERVICIO a TERMINADO
    Servicio servicio = orden.getServicio();
    servicio.setEstado(EstadoServicio.TERMINADO);
    servicio.setFechaDevolucionReal(LocalDate.now());
    servicioRepository.save(servicio);

    // Notificar cambios por WebSocket
    OrdenTrabajoEventDto ordenEvent = new OrdenTrabajoEventDto();
    ordenEvent.setTipoEvento("CAMBIO_ESTADO");
    ordenEvent.setOrdenTrabajoId(ordenGuardada.getId());
    ordenEvent.setServicioId(servicio.getId());
    ordenEvent.setNumeroServicio(servicio.getNumeroServicio());
    ordenEvent.setEstadoAnterior(estadoAnterior);
    ordenEvent.setEstadoNuevo(EstadoOrdenTrabajo.TERMINADA);
    webSocketNotificationService.notificarOrdenTrabajo(ordenEvent);

    ServicioEventDto servicioEvent = new ServicioEventDto();
    servicioEvent.setTipo("CAMBIO_ESTADO");
    servicioEvent.setServicioId(servicio.getId());
    servicioEvent.setNumeroServicio(servicio.getNumeroServicio());
    webSocketNotificationService.notificarServicioActualizado(servicioMapper.toListDto(servicio));

    return ordenTrabajoMapper.toResponseDto(ordenGuardada);
}
```

---

## 🎯 RESUMEN DE CAMBIOS:

### Archivos creados: ✅
1. ✅ PresupuestoEventDto.java
2. ✅ OrdenTrabajoEventDto.java

### Archivos modificados: ✅
3. ✅ PresupuestoListDto.java
4. ✅ WebSocketConfig.java
5. ✅ WebSocketNotificationService.java

### Archivos pendientes de modificar:
6. ⏳ ServicioServiceImpl.java
7. ⏳ PresupuestoController.java
8. ⏳ PresupuestoService.java (interface)
9. ⏳ PresupuestoServiceImpl.java
10. ⏳ OrdenTrabajoController.java
11. ⏳ OrdenTrabajoService.java (interface)
12. ⏳ OrdenTrabajoServiceImpl.java

---

## 📝 NOTAS IMPORTANTES:

1. **Excepciones personalizadas**: Asegúrate de que existan:
   - `PresupuestoNotFoundException`
   - `OrdenTrabajoNotFoundException`
   - `EmpleadoNotFoundException`

   Si no existen, créalas o usa `RuntimeException` temporalmente.

2. **Mappers**: Asegúrate de tener:
   - `presupuestoMapper.toResponseDto()`
   - `presupuestoMapper.toListDto()` (si no existe, créalo)
   - `ordenTrabajoMapper.toResponseDto()`
   - `ordenTrabajoMapper.toListDto()` (si no existe, créalo)
   - `servicioMapper.toListDto()`

3. **Repositorios**: Ya deberían existir, pero verifica que tengas inyectados todos los que necesitas.

4. **Testing**: Después de implementar todo, prueba:
   - Crear un servicio → Verificar que se cree el presupuesto automáticamente
   - Aprobar un presupuesto → Verificar que el servicio cambie a APROBADO y se cree la orden
   - Iniciar una orden → Verificar que el servicio cambie a EN_REPARACION
   - Terminar una orden → Verificar que el servicio cambie a TERMINADO

---

## 🚀 ORDEN SUGERIDO DE IMPLEMENTACIÓN:

1. Primero: ServicioServiceImpl (auto-crear presupuesto)
2. Segundo: PresupuestoService + PresupuestoServiceImpl (métodos)
3. Tercero: PresupuestoController (endpoints)
4. Cuarto: OrdenTrabajoService + OrdenTrabajoServiceImpl (métodos)
5. Quinto: OrdenTrabajoController (endpoints)

---

**¡Con esto el backend estará completo y listo para que el frontend pueda trabajar con los tableros!**
