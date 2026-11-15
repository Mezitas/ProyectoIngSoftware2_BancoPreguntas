package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.LoginRequestDTO;
import co.edu.unicauca.user.dto.LoginResponseDTO;
import co.edu.unicauca.user.service.UserService;
import co.edu.unicauca.user.service.KeycloakTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final KeycloakTokenService keycloak;

    public AuthController(UserService userService, KeycloakTokenService keycloak) {
        this.userService = userService;
        this.keycloak = keycloak;
    }

    /**
     * Intercambia las credenciales con Keycloak y agrega el perfil local requerido por las APIs.
     * Devuelve 401 cuando el proveedor rechaza las credenciales.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return keycloak.authenticate(request.username(), request.password())
                .map(identity -> {
                    var user = userService.synchronizeProfile(identity.username(), identity.fullName(),
                            identity.email(), identity.roles());
                    return ResponseEntity.ok(new LoginResponseDTO(
                            true, user, "Autenticación exitosa", identity.accessToken()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponseDTO(false, null, "Credenciales incorrectas", null)));
    }
}
