package com.gamesUP.gamesUP.security;

import com.gamesUP.gamesUP.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenService tokens;
    private final UserRepository users;

    public JwtAuthFilter(JwtTokenService tokens, UserRepository users) {
        this.tokens = tokens; this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String jwt = auth.substring(7);
            try {
                var claims = tokens.parse(jwt).getPayload();
                String email = claims.getSubject();
                String role = String.valueOf(claims.get("role"));
                if (users.findByEmail(email).isPresent()) {
                    GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_" + role);
                    Authentication a = new UsernamePasswordAuthenticationToken(email, null, List.of(ga));
                    SecurityContextHolder.getContext().setAuthentication(a);
                }
            } catch (Exception ignored) {}
        }
        chain.doFilter(request, response);
    }
}
