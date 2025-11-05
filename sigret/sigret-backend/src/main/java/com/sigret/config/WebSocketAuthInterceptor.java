package com.sigret.config;

import com.sigret.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Intentar obtener el token de diferentes lugares
            String token = null;

            // 1. Intentar desde el header 'Authorization'
            List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String bearerToken = authorizationHeaders.get(0);
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7);
                }
            }

            // 2. Si no está en Authorization, intentar desde query parameter 'token'
            if (token == null) {
                List<String> tokenHeaders = accessor.getNativeHeader("token");
                if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                    token = tokenHeaders.get(0);
                }
            }

            // Validar el token
            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String rol = jwtUtil.getRolFromToken(token);
                Long empleadoId = jwtUtil.getEmpleadoIdFromToken(token);

                // Crear lista de autoridades
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (rol != null) {
                    // Agregar el rol con prefijo ROLE_ si no lo tiene
                    String roleWithPrefix = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;
                    authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                }

                // Crear el token de autenticación
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // Agregar información adicional al accessor para uso posterior
                accessor.setUser(authentication);
                accessor.setHeader("username", username);
                accessor.setHeader("rol", rol);
                accessor.setHeader("empleadoId", empleadoId);

                // Establecer en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("WebSocket autenticado exitosamente para usuario: " + username + " con rol: " + rol);
            } else {
                // Token inválido o no presente
                System.err.println("WebSocket: Token JWT inválido o no presente");
                throw new IllegalArgumentException("Token JWT inválido o no presente");
            }
        }

        return message;
    }
}
