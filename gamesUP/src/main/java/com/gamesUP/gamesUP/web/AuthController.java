package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.security.JwtTokenService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegisterRequest(@Email String email, @NotBlank String password) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String role) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtTokenService tokens;

    public AuthController(UserRepository u, PasswordEncoder e, JwtTokenService t){
        this.users=u; this.encoder=e; this.tokens=t;
    }

    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest in){
        users.findByEmail(in.email()).ifPresent(u -> { throw new IllegalArgumentException("Email already used"); });
        User u = new User();
        u.setEmail(in.email());
        u.setPasswordHash(encoder.encode(in.password())); // <-- adapte si ton champ s'appelle 'password'
        u.setRole(Role.CLIENT);
        users.save(u);
        return new TokenResponse(tokens.generate(u.getEmail(), u.getRole().name()), u.getRole().name());
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest in){
        User u = users.findByEmail(in.email()).orElseThrow(() -> new IllegalArgumentException("Bad credentials"));
        // adapte si ton champ est 'password'
        if (!encoder.matches(in.password(), u.getPasswordHash())) throw new IllegalArgumentException("Bad credentials");
        return new TokenResponse(tokens.generate(u.getEmail(), u.getRole().name()), u.getRole().name());
    }
}
