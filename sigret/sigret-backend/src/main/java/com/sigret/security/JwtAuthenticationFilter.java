package com.sigret.security;


import com.sigret.services.CustomUserDetailsService;
import com.sigret.services.TokenBlacklistService;
import com.sigret.utilities.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends  OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token está en el formato "Bearer token". Remover Bearer palabra y obtener solo el Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);

            if (tokenBlacklistService.isBlacklisted(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token invalidado por logout\"}");
                return;
            }

            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                // Log solo en nivel DEBUG para evitar spam en logs de producción
                logger.error("Token JWT inválido o expirado: {}", e);
                // Si hay un token pero es inválido, devolver 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token invalido o expirado\"}");
                return;
            }
        }

        // Una vez que obtenemos el token, validamos
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Si el token es válido, configuramos Spring Security para autenticar manualmente al usuario
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Después de configurar Authentication en el contexto, especificamos que el usuario actual está autenticado
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    // Token inválido
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Token invalido\"}");
                    return;
                }
            } catch (Exception e) {
                // Log solo en nivel DEBUG para evitar spam en logs de producción
                logger.error("Error validando token: {}", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Error validando token\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
