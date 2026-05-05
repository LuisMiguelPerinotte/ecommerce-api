package com.java.luismiguel.ecommerce_api.application.auth;

import com.java.luismiguel.ecommerce_api.api.dto.auth.request.ChangePasswordRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.LoginRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RefreshRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RegisterRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.AuthResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.UserResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.*;
import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtProperties;
import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("registerNewUser")
    class RegisterNewUser {
        UUID userId;
        RegisterRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            requestDTO = new RegisterRequestDTO("Username", "email@email.com", "password123");
        }

        @Test
        @DisplayName("Should Register New User Successfully")
        void shouldRegisterNewUserSuccessfully() {
            User user = User.builder()
                    .username(requestDTO.username())
                    .build();

            User savedUser = User.builder()
                    .userId(userId)
                    .username(requestDTO.username())
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponseDTO result = authService.registerNewUser(requestDTO);

            assertThat(result.userId()).isNotNull();
            assertThat(result.username()).isEqualTo(requestDTO.username());
            verify(userRepository, times(1)).save(any(User.class));
        }


        @Test
        @DisplayName("Should Throw Exception When User Email Is Already Registered")
        void shouldThrowExceptionWhenUserEmailAlreadyRegistered() {
            when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(""));

            assertThrows(UserEmailAlreadyRegisteredException.class, () -> {
                authService.registerNewUser(requestDTO);
            });
        }
    }


    @Nested
    @DisplayName("userLogin")
    class UserLogin {
        LoginRequestDTO requestDTO;
        UUID userId;

        @BeforeEach
        void setUp() {
            requestDTO = new LoginRequestDTO("email@email.com", "password123");
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Account Is Deactivated")
        void shouldThrowExceptionWhenAccountIsDeactivated() {
            User user = User.builder()
                    .active(false)
                    .build();

            var authMock = mock(Authentication.class);

            given(authMock.getPrincipal()).willReturn(user);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authMock);

            assertThrows(UserAccountDeactivateException.class, () -> {
                authService.userLogin(requestDTO);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Invalid Credentials")
        void shouldThrowExceptionWhenInvalidCredentials() {
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Invalid username or password"));

            assertThrows(InvalidCredentialsException.class, () -> {
                authService.userLogin(requestDTO);
            });
        }


        @Test
        @DisplayName("Should Login Successfully")
        void shouldLoginSuccessfully() {
            User user = User.builder()
                    .userId(userId)
                    .email("email@email.com")
                    .password("encodedPassword")
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .active(true)
                    .build();

            String accessToken = "123";
            String refreshToken = "1234";
            Long expiration = 3600000L;

            var authMock = mock(Authentication.class);

            given(authMock.getPrincipal()).willReturn(user);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authMock);
            given(jwtService.generateToken(user)).willReturn(accessToken);
            given(jwtService.generateRefreshToken(user)).willReturn(refreshToken);
            given(jwtProperties.getExpiration()).willReturn(expiration);

            AuthResponseDTO result = authService.userLogin(requestDTO);

            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresIn()).isEqualTo(3600L);

            then(refreshTokenService).should().saveRefreshToken(userId, refreshToken);
        }
    }


    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {
        String email;
        UUID userId;
        RefreshRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            email = "email@emai.com";
            userId = UUID.randomUUID();
            requestDTO = new RefreshRequestDTO("123");

            given(jwtService.validateRefreshToken(requestDTO.refreshToken()))
                    .willReturn(email);
        }

        @Test
        @DisplayName("Should Throw Exception When User Not Found")
        void shouldThrowExceptionWhenUserNotFound() {
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> {
                authService.refreshToken(requestDTO);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When User Account Is Deactivated")
        void shouldThrowExceptionWhenUserAccountIsDeactivated() {
            User user = User.builder()
                    .userId(userId)
                    .active(false)
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            assertThrows(UserAccountDeactivateException.class, () -> {
                authService.refreshToken(requestDTO);
            });

            then(refreshTokenService).should().deleteRefreshToken(user.getUserId());
        }


        @Test
        @DisplayName("Should Throw Exception When Invalid Refresh Token")
        void shouldThrowExceptionWhenInvalidRefreshToken() {
            User user = User.builder()
                    .userId(userId)
                    .active(true)
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(refreshTokenService.isValid(user.getUserId(), requestDTO.refreshToken()))
                    .willReturn(false);

            assertThrows(InvalidRefreshTokenException.class, () -> {
                authService.refreshToken(requestDTO);
            });
        }


        @Test
        @DisplayName("Should Generate New Access Token")
        void shouldGenerateNewAccessToken() {
            User user = User.builder()
                    .userId(userId)
                    .active(true)
                    .build();

            String newRefreshToken = "123456";
            String newAccessToken = "12345";
            Long expiration = 3600000L;

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(refreshTokenService.isValid(user.getUserId(), requestDTO.refreshToken()))
                    .willReturn(true);
            given(jwtService.generateRefreshToken(user)).willReturn(newRefreshToken);
            given(jwtService.generateToken(user)).willReturn(newAccessToken);
            given(jwtProperties.getExpiration()).willReturn(expiration);

            AuthResponseDTO result = authService.refreshToken(requestDTO);

            assertThat(result.accessToken()).isEqualTo(newAccessToken);
            assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresIn()).isEqualTo(3600L);

            then(refreshTokenService).should().saveRefreshToken(userId, newRefreshToken);
        }
    }


    @Nested
    @DisplayName("loggedUser")
    class LoggedUser {
        UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return Correctly Dto")
        void shouldReturnCorrectlyDto() {
            String username = "username";
            String email = "email@email.com";
            UserRole role = UserRole.ROLE_CUSTOMER;
            LocalDateTime createdAt = LocalDateTime.now();

            User user = User.builder()
                    .userId(userId)
                    .username(username)
                    .email(email)
                    .userRole(role)
                    .createdAt(createdAt)
                    .build();

            UserResponseDTO result = authService.loggedUser(user);

            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo(username);
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.userRole()).isEqualTo(role);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }
    }


    @Nested
    @DisplayName("changePassword")
    class ChangePassword {
        String email;
        ChangePasswordRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            email = "email@email.com";
            requestDTO = new ChangePasswordRequestDTO("password123", "123password");
        }

        @Test
        @DisplayName("Should Throw Exception When User Not Found")
        void shouldThrowExceptionWhenUserNotFound() {
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> {
                authService.changePassword(requestDTO, email);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Current Password Is Incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
            User user = User.builder()
                    .password("password123")
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())).willReturn(false);

            assertThrows(InvalidPasswordException.class, () -> {
                authService.changePassword(requestDTO, email);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When New Password Is Equal To The Current")
        void shouldThrowExceptionWhenNewPasswordIsEqualToTheCurrent() {
            User user = User.builder()
                    .password("password123")
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())).willReturn(true);
            given(passwordEncoder.matches(requestDTO.newPassword(), user.getPassword())).willReturn(true);

            assertThrows(PasswordUnchangedException.class, () -> {
                authService.changePassword(requestDTO, email);
            });
        }


        @Test
        @DisplayName("Should Change Password Successfully")
        void shouldChangePasswordSuccessfully() {
            User user = User.builder()
                    .email(email)
                    .password("password123")
                    .build();

            String newEncodedPassword = "encodedPassword";

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())).willReturn(true);
            given(passwordEncoder.matches(requestDTO.newPassword(), user.getPassword())).willReturn(false);

            given(passwordEncoder.encode(requestDTO.newPassword())).willReturn(newEncodedPassword);

            authService.changePassword(requestDTO, email);

            assertThat(user.getPassword()).isEqualTo(newEncodedPassword);
            then(userRepository).should().save(user);
        }
    }


    @Nested
    @DisplayName("logout")
    class Logout {
        UUID userId;
        String email;
        String accessToken;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            email = "email@email.com";
            accessToken = "123";
        }

        @Test
        @DisplayName("Should Throw Exception When User Not Found")
        void shouldThrowExceptionWhenUserNotFound() {
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> {
                authService.logout(email, accessToken);
            });
        }


        @Test
        @DisplayName("Should Logout User Successfully")
        void shouldLogoutUserSuccessfully() {
            User user = User.builder()
                    .userId(userId)
                    .build();

            long expiration = 3600000L;

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(jwtProperties.getExpiration()).willReturn(expiration);

            authService.logout(email, accessToken);

            then(refreshTokenService).should().deleteRefreshToken(userId);
            then(refreshTokenService).should().blackListAccessToken(accessToken, expiration);
        }
    }
}