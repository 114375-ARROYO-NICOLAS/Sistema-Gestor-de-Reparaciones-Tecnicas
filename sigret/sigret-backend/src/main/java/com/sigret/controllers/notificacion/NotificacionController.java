package com.sigret.controllers.notificacion;

import com.sigret.dtos.notificacion.NotificacionDto;
import com.sigret.entities.Usuario;
import com.sigret.services.AuthService;
import com.sigret.services.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones de usuario")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private AuthService authService;

    @GetMapping
    @Operation(summary = "Obtener notificaciones recientes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionDto>> obtenerRecientes() {
        Long usuarioId = getUsuarioActualId();
        return ResponseEntity.ok(notificacionService.obtenerRecientes(usuarioId));
    }

    @GetMapping("/no-leidas/count")
    @Operation(summary = "Contar notificaciones no leídas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> contarNoLeidas() {
        Long usuarioId = getUsuarioActualId();
        return ResponseEntity.ok(notificacionService.contarNoLeidas(usuarioId));
    }

    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar notificación como leída")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        Long usuarioId = getUsuarioActualId();
        notificacionService.marcarComoLeida(id, usuarioId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/leer-todas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        Long usuarioId = getUsuarioActualId();
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    private Long getUsuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Usuario usuario = authService.getCurrentUser(username);
        return usuario.getId();
    }
}
