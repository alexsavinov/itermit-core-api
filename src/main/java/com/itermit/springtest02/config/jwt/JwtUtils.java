package com.itermit.springtest02.config.jwt;

import com.itermit.springtest02.model.entity.User;
import com.itermit.springtest02.service.implementation.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class JwtUtils {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Resource
    private UserDetailsService userDetailsService;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
//        String secret = Base64.getEncoder().encodeToString(this.jwtSecret.getBytes());
//        this.jwtSecret = Base64.getEncoder().encodeToString(this.jwtSecret1.getBytes());
//        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        User user = User.builder()
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
//                .name(userPrincipal.getUsername())
                .build();
        return generateTokenFromUser(user);
    }

    public String generateTokenFromUser(User user) {
        List<String> roles = new ArrayList<>();
        Map<String, Object> rolesClaim = new HashMap<>();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        userDetails.getAuthorities().forEach(a -> roles.add(a.getAuthority()));
        rolesClaim.put("roles", roles);

        long expirationDate = (new Date()).getTime() + jwtExpirationMs;

                String secret = Base64.getEncoder().encodeToString(this.jwtSecret.getBytes());
//        this.jwtSecret = Base64.getEncoder().encodeToString(this.jwtSecret1.getBytes());
        SecretKey secretKey1 = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setId(user.getId().toString())
                .setSubject(user.getUsername())
                .addClaims(rolesClaim)
                .setIssuedAt(new Date())
                .setExpiration(new Date(expirationDate)).signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(token).getPayload().getSubject();
//        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
//            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
