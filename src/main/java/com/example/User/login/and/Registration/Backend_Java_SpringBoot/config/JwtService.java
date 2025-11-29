package com.example.User.login.and.Registration.Backend_Java_SpringBoot.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;  // ❌ DEPRECATED - Remove this import
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // ✅ 256-bit (32-byte) Base64-encoded secret key for HMAC-SHA256 signing
    private static final String secret = "d0355e72a543556c14811d98d431b018b02d35c6fff3c92b4356d255c14643e1";

    /**
     * Extracts username (subject) from JWT token claims
     * @param token JWT token string
     * @return username/email from token subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);  // Generic claim extractor with subject function
    }

    /**
     * Generates JWT token with optional extra claims (roles, permissions)
     * @param extraClaims Additional claims like roles: {"roles": ["USER", "ADMIN"]}
     * @param userDetails Spring Security user details
     * @return Compact JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)                    // ✅ Adds custom claims (roles, permissions)
                .subject(userDetails.getUsername())     // ✅ Sets username/email as JWT subject
                .issuedAt(new Date(System.currentTimeMillis()))  // ✅ JWT issuance timestamp
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 24))  // ✅ 24-hour expiry
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // ❌ DEPRECATED - Fix: .signWith(getSigningKey())
                .compact();  // ✅ Creates final compact JWT string
    }

    /**
     * Validates token: checks username matches AND token not expired
     * @param token JWT token
     * @param userDetails User from database
     * @return true if token valid for this user
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);  // Extract username from token
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);  // ✅ Username match + not expired
    }

    /**
     * Checks if token is expired
     * @param token JWT token
     * @return true if CURRENT TIME > token expiration
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());  // ✅ Compares token expiry vs now
    }

    /**
     * Extracts expiration Date claim from token
     * @param token JWT token
     * @return Expiration Date from claims
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);  // Generic extractor with expiration function
    }

    /**
     * Generic claim extractor using functional interface
     * @param TOKEN JWT token
     * @param claimsResolver Function to extract specific claim type
     * @param <T> Claim type (String, Date, etc.)
     * @return Extracted claim value
     */
    public <T> T extractClaim(String TOKEN, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaimsJWT(TOKEN);  // Parse full claims first
        return claimsResolver.apply(claims);  // Apply function to get specific claim
    }

    /**
     * Convenience method: generates token with no extra claims
     * @param userDetails Spring Security user
     * @return Simple JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);  // Delegates to full method with empty claims
    }

    /**
     * Parses and validates JWT token, returns claims payload
     * @param token JWT token string
     * @return Claims payload (subject, expiration, custom claims)
     */
    private Claims extractClaimsJWT(String token) {
        return Jwts.parser()                    // ✅ Modern parser builder
                .verifyWith((SecretKey) getSigningKey())     // ✅ Verifies signature with secret key (CAST NEEDED)
                .build()                                     // ✅ Builds immutable parser
                .parseSignedClaims(token)                    // ✅ Parses signed JWT
                .getPayload();                               // ✅ Returns claims payload
    }

    /**
     * Creates HMAC-SHA256 signing key from Base64 secret
     * @return SecretKey for signing/verification
     */
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);  // ✅ Decodes Base64 secret to bytes
        return Keys.hmacShaKeyFor(keyBytes);  // ✅ Creates secure HMAC key
    }
}
