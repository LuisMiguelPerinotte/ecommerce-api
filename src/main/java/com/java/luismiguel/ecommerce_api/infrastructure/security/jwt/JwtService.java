package com.java.luismiguel.ecommerce_api.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService {
    private final JwtProperties jwtProperties;
    private final Algorithm algorithm;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());;
    }

    public String generateToken(User user) {
        return JWT.create()
                .withIssuer("ecommerce-api")
                .withClaim("userId", user.getUserId().toString())
                .withSubject(user.getEmail())
                .withClaim("role", user.getUserRole().name())
                .withExpiresAt(Instant.now().plusMillis(jwtProperties.getExpiration()))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }


    public String generateRefreshToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()))
                .withIssuedAt(Instant.now())
                .withIssuer("ecommerce-api")
                .sign(algorithm);
    }


    public Optional<JwtUserData> validateToken(String token) {
        try {
            DecodedJWT decode = JWT.require(algorithm)
                    .withIssuer("ecommerce-api")
                    .build()
                    .verify(token);

            return Optional.of(JwtUserData.builder()
                    .userId(UUID.fromString(decode.getClaim("userId").asString()))
                    .email(decode.getSubject())
                    .userRole(UserRole.valueOf(decode.getClaim("role").asString()))
                    .build());

        } catch (TokenExpiredException e) {
            throw new CredentialsExpiredException("Expired Token!");

        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid Token!");
        }
    }

    public String validateRefreshToken(String refreshToken) {
        try{
            DecodedJWT decode = JWT.require(algorithm)
                    .withIssuer("ecommerce-api")
                    .build()
                    .verify(refreshToken);

            return decode.getSubject();

        } catch (TokenExpiredException e) {
            throw new CredentialsExpiredException("Expired Token!");

        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid Token!");
        }
    }
}
