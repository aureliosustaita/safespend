package com.safespend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityMs;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret:dev-secret-change-me-please-32chars-min-aaaaaaaa}") String secret,
            @Value("${app.security.jwt.expiration:3600000}") long validityMs,
            UserDetailsService userDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityMs = validityMs;
        this.userDetailsService = userDetailsService;
    }

    public String createToken(String username, Collection<? extends GrantedAuthority> roles) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + validityMs);
        String rolesStr = roles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesStr)
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try { Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token); return true; }
        catch (Exception e) { return false; }
    }

    public String getUsername(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return c.getSubject();
    }
}
