package com.java.luismiguel.ecommerce_api.application.auth;

import com.java.luismiguel.ecommerce_api.api.dto.request.ChangePasswordRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.LoginRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.RefreshRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.RegisterRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.AuthResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.UserResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.cart.Cart;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.*;
import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtProperties;
import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, JwtProperties jwtProperties, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponseDTO registerNewUser(RegisterRequestDTO registerRequestDTO) {
        User user = User.builder()
                .email(registerRequestDTO.email().toLowerCase().trim())
                .username(registerRequestDTO.username().trim())
                .password(passwordEncoder.encode(registerRequestDTO.password()))
                .userRole(UserRole.ROLE_CUSTOMER)
                .active(Boolean.TRUE)
                .build();

        Cart userCart = Cart.builder()
                .user(user)
                .build();

        try {
            user.setCart(userCart);
            User savedUser = userRepository.save(user);
            return new UserResponseDTO(
                    savedUser.getUserId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getUserRole(),
                    savedUser.getCreatedAt()
            );

        } catch (DataIntegrityViolationException exception) {
            throw new UserEmailAlreadyRegisteredException();
        }

    }


    public AuthResponseDTO userLogin(LoginRequestDTO loginRequestDTO) {
        try{
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequestDTO.email(),
                    loginRequestDTO.password()
            );

            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            User user = (User) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            refreshTokenService.saveRefreshToken(user.getUserId(), refreshToken);

            return new AuthResponseDTO(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    jwtProperties.getExpiration() / 1000
            );

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

    }


    public AuthResponseDTO refreshToken(RefreshRequestDTO refreshRequestDTO) {
        String email = jwtService.validateRefreshToken(refreshRequestDTO.refreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!refreshTokenService.isValid(user.getUserId(), refreshRequestDTO.refreshToken())) {
            throw new InvalidRefreshTokenException();
        }

        String newRefreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.saveRefreshToken(user.getUserId(), newRefreshToken);

        return new AuthResponseDTO(
                jwtService.generateToken(user),
                newRefreshToken,
                "Bearer",
                jwtProperties.getExpiration() / 1000
        );
    }


    public UserResponseDTO loggedUser(User user){
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRole(),
                user.getCreatedAt()
        );
    }


    public void changePassword(ChangePasswordRequestDTO changePasswordRequestDTO, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(changePasswordRequestDTO.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (passwordEncoder.matches(changePasswordRequestDTO.newPassword(), user.getPassword())) {
            throw new PasswordUnchangedException();
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequestDTO.newPassword()));
        userRepository.save(user);
    }


    public void logout(String email, String accessToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        refreshTokenService.deleteRefreshToken(user.getUserId());

        refreshTokenService.blackListAccessToken(
                accessToken,
                jwtProperties.getExpiration()
        );
    }
}
