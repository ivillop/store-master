package com.storemaster.security;

import com.storemaster.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtService jwt;
    private final AppUserRepository users;

    public JwtAuthFilter(JwtService jwt, AppUserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIX)) {
            jwt.parse(header.substring(PREFIX.length())).ifPresent(claims ->
                    users.findByUsername(claims.getSubject()).filter(u -> u.isEnabled()).ifPresent(u -> {
                        var auth = new UsernamePasswordAuthenticationToken(u.getUsername(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }));
        }
        chain.doFilter(request, response);
    }
}
