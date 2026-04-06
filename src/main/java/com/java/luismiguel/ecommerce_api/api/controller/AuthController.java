package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.auth.request.ChangePasswordRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.LoginRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RefreshRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RegisterRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.AuthResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.UserResponseDTO;
import com.java.luismiguel.ecommerce_api.application.auth.AuthService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Handles user authentication and token management")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    @RateLimiter(name = "critical")
    @Operation(summary = "Register User", description = "Validates input and registers a new user. Returns user data without sensitive fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid registration data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already registered (conflict)", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid
            @RequestBody RegisterRequestDTO registerRequestDTO
    ) {
        return new ResponseEntity<>(authService.registerNewUser(registerRequestDTO), HttpStatus.CREATED);
    }


    @PostMapping("/login")
    @RateLimiter(name = "critical")
    @Operation(summary = "User Login", description = "Authenticates the user and returns an access JWT and a refresh token upon successful authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    public ResponseEntity<AuthResponseDTO> loginUser(
            @Valid
            @RequestBody LoginRequestDTO loginRequestDTO
    ) {
        return new ResponseEntity<>(authService.userLogin(loginRequestDTO), HttpStatus.OK);
    }


    @PostMapping("/refresh")
    @RateLimiter(name = "critical")
    @Operation(summary = "Refresh Token", description = "Refresh the access token using a valid refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid refresh token payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid or expired refresh token", content = @Content)
    })
    public ResponseEntity<AuthResponseDTO> refreshLoginToken(
            @Valid
            @RequestBody RefreshRequestDTO refreshRequestDTO
    ) {
        System.out.println("Chegou no controller");
        return new ResponseEntity<>(authService.refreshToken(refreshRequestDTO), HttpStatus.OK);
    }


    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @RateLimiter(name = "public-api")
    @Operation(summary = "Get Logged User", description = "Returns the authenticated user's profile information based on the JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User data returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class)))
    })
    public ResponseEntity<UserResponseDTO> getLoggedUser(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(authService.loggedUser(user), HttpStatus.OK);
    }


    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @RateLimiter(name = "critical")
    @Operation(summary = "Change Password", description = "Change the authenticated user's password. Requires current password for verification.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid password payload", content = @Content)
    })
    public ResponseEntity<Void> changeUserPassword(
            @Valid
            @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        authService.changePassword(changePasswordRequestDTO, user.getEmail());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @RateLimiter(name = "critical")
    @Operation(summary = "User Logout", description = "Invalidates the current refresh token and logs the user out.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logged out (no content)"),
            // Authentication errors are handled globally by the SecurityScheme; no 401 documented here
    })
    public ResponseEntity<Void> userLogout(
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        String accessToken = request.getHeader("Authorization").substring(7);
        authService.logout(user.getEmail(), accessToken);
        return ResponseEntity.noContent().build();
    }
}
