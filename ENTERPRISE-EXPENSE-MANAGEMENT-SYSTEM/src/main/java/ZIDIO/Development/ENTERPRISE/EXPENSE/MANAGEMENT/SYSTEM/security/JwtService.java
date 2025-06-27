package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User; // Your User entity
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority; // Import this
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List; // Import this
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors; // Import this

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // This method is usually called by your AuthService during login
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        // Check if the provided UserDetails is actually an instance of your custom User entity
        if (userDetails instanceof User customUserEntity) {
            extraClaims.put("userId", customUserEntity.getId());
            // Extract roles from your User entity's getAuthorities() method
            List<String> rolesList = customUserEntity.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            extraClaims.put("roles", rolesList); // <<<<< ADD ROLES HERE
            // You can add other custom claims like firstName, lastName if needed
            // extraClaims.put("firstName", customUserEntity.getFirstName());
        } else {
            // Fallback if UserDetails is not your custom User entity
            // (e.g., if using Spring's InMemoryUserDetailsManager or a different UserDetails impl)
            // This part might not include userId if it's not available on the generic UserDetails
            List<String> rolesList = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            extraClaims.put("roles", rolesList); // <<<<< ADD ROLES HERE
        }
        // The `userRepository.findByUsername(username)` in your SecurityConfig's userDetailsService
        // should return your `User` entity which implements `UserDetails`.
        // So, the `instanceof User` check above should normally pass.

        return buildToken(extraClaims, userDetails.getUsername()); // Pass username explicitly
    }

    // Renamed to avoid confusion if you had another method with Map, UserDetails
    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        // ... (your existing validation logic)
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}