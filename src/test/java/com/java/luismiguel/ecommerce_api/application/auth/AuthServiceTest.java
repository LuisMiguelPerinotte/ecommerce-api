package com.java.luismiguel.ecommerce_api.application.auth;

import com.java.luismiguel.ecommerce_api.api.dto.auth.request.ChangePasswordRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.LoginRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RefreshRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RegisterRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.AuthResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.UserResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.cart.Cart;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.*;
import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtProperties;
import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
            // given
            User savedUser = User.builder()
                    .userId(userId)
                    .username(requestDTO.username())
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(userRepository.existsByEmail(requestDTO.email())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            authService.registerNewUser(requestDTO);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            then(userRepository).should().save(userCaptor.capture());

            User userToSave = userCaptor.getValue();

            assertThat(userToSave.getEmail()).isEqualTo("email@email.com");
            assertThat(userToSave.getUsername()).isEqualTo("Username");
            assertThat(userToSave.getUserRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
            assertThat(userToSave.getActive()).isTrue();
            assertThat(userToSave.getCart()).isNotNull();
            assertThat(userToSave.getCart().getUser()).isSameAs(userToSave);
        }


        @Test
        @DisplayName("Should Encode Password Successfully")
        void shouldEncodePasswordSuccessfully() {
            // given
            User savedUser = User.builder()
                    .userId(userId)
                    .username(requestDTO.username())
                    .email(requestDTO.email())
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(userRepository.existsByEmail(requestDTO.email())).willReturn(false);
            given(passwordEncoder.encode(requestDTO.password())).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            authService.registerNewUser(requestDTO);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userCaptor.capture());

            User userToSave = userCaptor.getValue();

            assertThat(userToSave.getPassword()).isEqualTo("encoded-password");
            assertThat(userToSave.getPassword()).isNotEqualTo("password123");
            then(passwordEncoder).should().encode("password123");
        }


        @Test
        @DisplayName("Should Create User Cart Automatically")
        void shouldCreateUserCartAutomatically() {
            // given
            User savedUser = User.builder()
                    .userId(userId)
                    .username(requestDTO.username())
                    .email(requestDTO.email())
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(userRepository.existsByEmail(requestDTO.email())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            authService.registerNewUser(requestDTO);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userCaptor.capture());

            User userToSave = userCaptor.getValue();

            assertThat(userToSave.getCart()).isNotNull();
            assertThat(userToSave.getCart().getUser()).isSameAs(userToSave);
        }



        @Test
        @DisplayName("Should Throw Exception When User Email Is Already Registered")
        void shouldThrowExceptionWhenUserEmailAlreadyRegistered() {
            // given
            given(userRepository.existsByEmail("email@email.com")).willReturn(true);

            // when + then
            UserEmailAlreadyRegisteredException exception = assertThrows(UserEmailAlreadyRegisteredException.class, () -> {
                authService.registerNewUser(requestDTO);
            });

            assertThat(exception.getMessage()).isEqualTo("E-mail is already registered!");
        }


        @Test
        @DisplayName("Should Throw Exception When Data Integrity Violation")
        void shouldThrowExceptionWhenDataIntegrityViolation() {
            // given
            given(userRepository.existsByEmail("email@email.com")).willReturn(false);
            given(userRepository.save(any(User.class)))
                    .willThrow(new DataIntegrityViolationException(""));

            // when + then
            UserRegistrationDataIntegrityException exception = assertThrows(UserRegistrationDataIntegrityException.class, () -> {
                authService.registerNewUser(requestDTO);
            });

            assertThat(exception.getMessage()).isEqualTo("Registration Data Integrity Violation!");
        }
    }


    @Nested
    @DisplayName("userLogin")
    class UserLogin {
        LoginRequestDTO requestDTO;
        UUID userId;

        @BeforeEach
        void setUp() {
            requestDTO = new LoginRequestDTO(
                    "  EMAIL@email.com  ",
                    "password123"
            );
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Account Is Deactivated")
        void shouldThrowExceptionWhenAccountIsDeactivated() {
            // given
            User user = User.builder()
                    .active(false)
                    .build();

            var authMock = mock(Authentication.class);

            given(authMock.getPrincipal()).willReturn(user);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authMock);

            // when + then
            UserAccountDeactivateException exception = assertThrows(UserAccountDeactivateException.class, () -> {
                authService.userLogin(requestDTO);
            });

            assertThat(exception.getMessage()).isEqualTo("User Account Has Been Deactivated!");
        }


        @Test
        @DisplayName("Should Throw Exception When Invalid Credentials")
        void shouldThrowExceptionWhenInvalidCredentials() {
            // given
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Invalid username or password"));

            // when + then
            InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
                authService.userLogin(requestDTO);
            });

            assertThat(exception.getMessage()).isEqualTo("Invalid E-mail or password!");
        }


        @Test
        @DisplayName("Should Login Successfully")
        void shouldLoginSuccessfully() {
            // given
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

            // when
            AuthResponseDTO result = authService.userLogin(requestDTO);

            // then
            assertThat(user.getEmail()).isEqualTo("email@email.com");

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
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when + then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                authService.refreshToken(requestDTO);
            });

            assertThat(exception.getMessage()).isEqualTo("User Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When User Account Is Deactivated")
        void shouldThrowExceptionWhenUserAccountIsDeactivated() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .active(false)
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // when + then
            assertThrows(UserAccountDeactivateException.class, () -> {
                authService.refreshToken(requestDTO);
            });

            then(refreshTokenService).should().deleteRefreshToken(user.getUserId());
        }


        @Test
        @DisplayName("Should Throw Exception When Invalid Refresh Token")
        void shouldThrowExceptionWhenInvalidRefreshToken() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .active(true)
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(refreshTokenService.isValid(user.getUserId(), requestDTO.refreshToken()))
                    .willReturn(false);

            // when + then
            InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class, () -> {
                authService.refreshToken(requestDTO);
            });

            assertThat(exception.getMessage()).isEqualTo("Invalid Refresh Token!");
        }


        @Test
        @DisplayName("Should Generate New Access Token")
        void shouldGenerateNewAccessToken() {
            // given
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

            // when
            AuthResponseDTO result = authService.refreshToken(requestDTO);

            // then
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
            // given
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

            // when
            UserResponseDTO result = authService.loggedUser(user);

            // then
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
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when + then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                authService.changePassword(requestDTO, email);
            });

            assertThat(exception.getMessage()).isEqualTo("User Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Current Password Is Incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
            // given
            User user = User.builder()
                    .password("password123")
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())).willReturn(false);

            // when + then
            InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
                authService.changePassword(requestDTO, email);
            });

            assertThat(exception.getMessage()).isEqualTo("Current password is incorrect!");
        }


        @Test
        @DisplayName("Should Throw Exception When New Password Is Equal To The Current")
        void shouldThrowExceptionWhenNewPasswordIsEqualToTheCurrent() {
            // given
            User user = User.builder()
                    .password("password123")
                    .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())).willReturn(true);
            given(passwordEncoder.matches(requestDTO.newPassword(), user.getPassword())).willReturn(true);

            // when + then
            PasswordUnchangedException exception = assertThrows(PasswordUnchangedException.class, () -> {
                authService.changePassword(requestDTO, email);
            });

            assertThat(exception.getMessage()).isEqualTo("New Password must be different from current!");
        }


        @Test
        @DisplayName("Should Change Password Successfully")
        void shouldChangePasswordSuccessfully() {
            // given
            User user = User.builder()
                    .email(email)
                    .password("password123")
                    .build();

            String newEncodedPassword = "encodedPassword";

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())).willReturn(true);
            given(passwordEncoder.matches(requestDTO.newPassword(), user.getPassword())).willReturn(false);

            given(passwordEncoder.encode(requestDTO.newPassword())).willReturn(newEncodedPassword);

            // when
            authService.changePassword(requestDTO, email);

            // then
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
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when + then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                authService.logout(email, accessToken);
            });

            assertThat(exception.getMessage()).isEqualTo("User Not Found!");
        }


        @Test
        @DisplayName("Should Logout User Successfully")
        void shouldLogoutUserSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            long expiration = 3600000L;

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(jwtProperties.getExpiration()).willReturn(expiration);

            // when
            authService.logout(email, accessToken);

            // then
            then(refreshTokenService).should().deleteRefreshToken(userId);
            then(refreshTokenService).should().blackListAccessToken(accessToken, expiration);
        }
    }
}