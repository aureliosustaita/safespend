package com.safespend.web;

import com.safespend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwt) {
        this.authManager = authManager; this.jwt = jwt;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwt.createToken(authentication.getName(), authentication.getAuthorities());
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("issuedAt", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
