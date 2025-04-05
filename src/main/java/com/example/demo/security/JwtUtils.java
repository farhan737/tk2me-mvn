package com.example.demo.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Helper method to get a secure key for JWT operations
     */
    private byte[] getSecureKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        // Ensure key is at least 512 bits (64 bytes) for HS512
        if (keyBytes.length < 64) {
            // Pad the key to 64 bytes if it's too short
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
            keyBytes = paddedKey;
        }
        return keyBytes;
    }
    
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(getSecureKey()))
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(getSecureKey()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            log.info("Validating JWT token: {}...", authToken.substring(0, Math.min(10, authToken.length())));
            log.info("JWT secret length: {}", jwtSecret.length());
            log.info("JWT secure key length: {}", getSecureKey().length);
            
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(getSecureKey()))
                .build()
                .parseClaimsJws(authToken);
                
            log.info("JWT token validated successfully");
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            log.error("Token: {}...", authToken.substring(0, Math.min(10, authToken.length())));
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            log.error("Token: {}...", authToken.substring(0, Math.min(10, authToken.length())));
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            log.error("Token: {}...", authToken.substring(0, Math.min(10, authToken.length())));
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            log.error("Token: {}...", authToken.substring(0, Math.min(10, authToken.length())));
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage());
            log.error("Token: {}...", authToken.substring(0, Math.min(10, authToken.length())));
            e.printStackTrace();
        }

        return false;
    }
}
