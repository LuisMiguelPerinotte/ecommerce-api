package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.request.ChangePasswordRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.LoginRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.RefreshRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.RegisterRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.AuthResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.UserResponseDTO;
import com.java.luismiguel.ecommerce_api.application.auth.AuthService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Responsável por gerenciar as requisições de autenticação de usuários.")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    @Operation(summary = "Register (POST)", description = "Valida os dados e registra um novo usuário no sistema.")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid
            @RequestBody RegisterRequestDTO registerRequestDTO
    ) {
        return new ResponseEntity<>(authService.registerNewUser(registerRequestDTO), HttpStatus.CREATED);
    }


    @PostMapping("/login")
    @Operation(summary = "Login (POST)", description = "Realiza o login do usuário, valida as credenciais e gera um token JWT se a autenticação for bem-sucedida." )
    public ResponseEntity<AuthResponseDTO> loginUser(
            @Valid
            @RequestBody LoginRequestDTO loginRequestDTO
    ) {
        return new ResponseEntity<>(authService.userLogin(loginRequestDTO), HttpStatus.OK);
    }


    @PostMapping("/refresh")
    @Operation(summary = "refresh token (POST)", description = "Renova o token com um refreshToken válido")
    public ResponseEntity<AuthResponseDTO> refreshLoginToken(
            @Valid
            @RequestBody RefreshRequestDTO refreshRequestDTO
    ) {
        System.out.println("Chegou no controller");
        return new ResponseEntity<>(authService.refreshToken(refreshRequestDTO), HttpStatus.OK);
    }


    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "get logged user (GET)", description = "Retorna dados do usuário logado")
    public ResponseEntity<UserResponseDTO> getLoggedUser(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(authService.loggedUser(user), HttpStatus.OK);
    }


    @PostMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Change password (POST)", description = "Muda a senha (Precisa da senha Atual)")
    public ResponseEntity<Void> changeUserPassword(
            @Valid
            @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        authService.changePassword(changePasswordRequestDTO, user.getEmail());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/logout")
    @PreAuthorize("hasRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "User Logout (POST)", description = "Inválida o refresh token atual!")
    public ResponseEntity<Void> userLogout(
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        String accessToken = request.getHeader("Authorization").substring(7);
        authService.logout(user.getEmail(), accessToken);
        return ResponseEntity.noContent().build();
    }
}
