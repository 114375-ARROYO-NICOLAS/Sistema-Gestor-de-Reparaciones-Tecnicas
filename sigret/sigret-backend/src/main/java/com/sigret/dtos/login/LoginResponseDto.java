package com.sigret.dtos.login;

import com.sigret.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo userInfo;

    public LoginResponseDto(String token, String refreshToken, Long expirationTime, UserInfo userInfo) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expirationTime;
        this.userInfo = userInfo;
    }


    // Clase interna para información del usuario
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nombreCompleto;
        private RolUsuario rol;
        private Long empleadoId;

    }
}
