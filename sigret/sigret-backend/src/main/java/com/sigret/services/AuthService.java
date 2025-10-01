package com.sigret.services;

import com.sigret.dtos.login.LoginRequestDto;
import com.sigret.dtos.login.LoginResponseDto;
import com.sigret.dtos.login.RefreshTokenRequestDto;
import com.sigret.entities.Usuario;
import com.sigret.repositories.UsuarioRepository;
import com.sigret.security.CustomUserDetails;
import com.sigret.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Usuario usuario = userDetails.getUsuario();

            // Actualizar último login
            usuario.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(usuario);

            // Generar tokens con claims adicionales
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("rol", usuario.getRol().name());
            extraClaims.put("empleadoId", usuario.getEmpleado().getId());
            extraClaims.put("nombreCompleto", usuario.getNombreCompleto());

            String token = jwtUtil.generateToken(userDetails, extraClaims);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Crear response con información del usuario
            LoginResponseDto.UserInfo userInfo = new LoginResponseDto.UserInfo(
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNombreCompleto(),
                    usuario.getRol(),
                    usuario.getEmpleado().getId()
            );

            return new LoginResponseDto(
                    token,
                    refreshToken,
                    jwtUtil.getExpirationTime(),
                    userInfo
            );

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    public LoginResponseDto refreshToken(RefreshTokenRequestDto refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        try {
            // Validar que sea un refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("Token inválido");
            }

            String username = jwtUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                Usuario usuario = customUserDetails.getUsuario();

                // Generar nuevo access token
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("rol", usuario.getRol().name());
                extraClaims.put("empleadoId", usuario.getEmpleado().getId());
                extraClaims.put("nombreCompleto", usuario.getNombreCompleto());

                String newToken = jwtUtil.generateToken(userDetails, extraClaims);

                LoginResponseDto.UserInfo userInfo = new LoginResponseDto.UserInfo(
                        usuario.getId(),
                        usuario.getUsername(),
                        usuario.getNombreCompleto(),
                        usuario.getRol(),
                        usuario.getEmpleado().getId()
                );

                return new LoginResponseDto(
                        newToken,
                        refreshToken, // Mantener el mismo refresh token
                        jwtUtil.getExpirationTime(),
                        userInfo
                );
            } else {
                throw new IllegalArgumentException("Refresh token expirado o inválido");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al procesar refresh token: " + e.getMessage());
        }
    }

    public Usuario getCurrentUser(String username) {
        return usuarioRepository.findByUsernameWithDetails(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}