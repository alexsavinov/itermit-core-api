package com.itermit.core.config.jwt;

import com.itermit.core.service.implementation.UserDetailsImpl;
import com.itermit.core.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

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


    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        User user = User.builder()
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
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

        return Jwts.builder()
                .id(user.getId().toString())
                .subject(user.getUsername())
                .claims(rolesClaim)
                .issuedAt(new Date())
                .expiration(new Date(expirationDate))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
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
