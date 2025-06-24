package com.hotel.chatbox.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors; // Added for more detailed role logging


@Service
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long EXPIRATION_TIME;

    private SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.SECRET_KEY = secret;
        this.signingKey = getSigningKey();
        // Log the decoded key length to ensure it's correct for HS512 (should be 64)
        logger.info("JWT Secret Key Decoded Length: {} bytes", signingKey.getEncoded().length);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            logger.debug("Attempting to parse and validate token: {}", token);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.debug("Token parsed successfully. Subject: {}, Expiration: {}", claims.getSubject(), claims.getExpiration());
            return claims;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature for token: {}. Error: {}", token, e.getMessage(), e);
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}. Token expiration: {}. Current time according to server: {}",
                        token, e.getClaims().getExpiration(), new Date());
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}. Error: {}", token, e.getMessage(), e);
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date(); // Get current time
        logger.debug("Checking token expiration. Token expires: {}, Current server time: {}", expiration, now);
        return expiration.before(now);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Add more detailed logging for validation
        logger.debug("Validating token for user: {}. Extracted username: {}", userDetails.getUsername(), username);
        boolean usernameMatches = username.equals(userDetails.getUsername());
        boolean notExpired = !isTokenExpired(token);

        logger.debug("Username matches: {}. Token not expired: {}", usernameMatches, notExpired);

        return (usernameMatches && notExpired);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Collect all roles into a list
        claims.put("roles", userDetails.getAuthorities().stream()
                                    .map(a -> a.getAuthority())
                                    .collect(Collectors.toList()));
        
        logger.debug("Generating token for user: {}. Roles: {}", userDetails.getUsername(), claims.get("roles"));
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long currentTimeMillis = System.currentTimeMillis();
        Date issuedAt = new Date(currentTimeMillis);
        Date expiration = new Date(currentTimeMillis + EXPIRATION_TIME);

        logger.info("Token Creation - Issued At: {} ({})", issuedAt, issuedAt.getTime());
        logger.info("Token Creation - Expiration: {} ({})", expiration, expiration.getTime());
        logger.info("Token Creation - Current System.currentTimeMillis(): {}", currentTimeMillis);
        logger.info("Token Creation - Configured EXPIRATION_TIME: {}", EXPIRATION_TIME);


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}