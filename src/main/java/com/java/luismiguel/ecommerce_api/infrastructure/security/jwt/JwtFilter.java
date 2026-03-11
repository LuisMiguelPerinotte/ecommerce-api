package com.java.luismiguel.ecommerce_api.infrastructure.security.jwt;

import com.java.luismiguel.ecommerce_api.application.auth.RefreshTokenService;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.auth.UserNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {
        String authorizedHeader = request.getHeader("Authorization");

        if (Strings.isNotEmpty(authorizedHeader) && authorizedHeader.startsWith("Bearer ")) {
            String token = authorizedHeader.substring(7);

            try {
                if (refreshTokenService.isBlacklisted(token)){
                    throw new BadCredentialsException("Invalid Token!");
                }

                Optional<JwtUserData> optUser = jwtService.validateToken(token);
                if (optUser.isEmpty()){
                    throw new BadCredentialsException("Invalid Token!");
                }

                JwtUserData userData = optUser.get();
                UserDetails user = userDetailsService.loadUserByUsername(userData.email());
                var authority = new SimpleGrantedAuthority(userData.userRole().name());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, List.of(authority));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (CredentialsExpiredException e) {
                request.setAttribute("exception", "Expired Token!");
                request.setAttribute("status", HttpServletResponse.SC_UNAUTHORIZED);

            } catch (BadCredentialsException e) {
                request.setAttribute("exception", "Invalid Token!");
                request.setAttribute("status", HttpServletResponse.SC_UNAUTHORIZED);

            } catch (UserNotFoundException e) {
                request.setAttribute("exception", "User Not Found!");
                request.setAttribute("status", HttpServletResponse.SC_NOT_FOUND);

            } catch (Exception e) {
                request.setAttribute("exception", "Authentication Error!");
                request.setAttribute("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        filterChain.doFilter(request, response);
    }
}