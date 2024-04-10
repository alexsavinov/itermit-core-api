package com.itermit.core.service.implementation;

import com.itermit.core.exception.RefreshTokenExpiredException;
import com.itermit.core.repository.RefreshTokenRepository;
import com.itermit.core.model.entity.RefreshToken;
import com.itermit.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException(
                    token.getToken(),
                    "Refresh token was expired. Please make a new login request"
            );
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public int deleteAllByUserId(Long userId) {
        return refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional
    @Scheduled(cron = "0 */1 * * * ?")
    public void deleteExpiredTokens() {
        List<RefreshToken> tokenList = refreshTokenRepository.findAll();
        tokenList.forEach(refreshToken -> {
            if (Instant.now().isAfter(refreshToken.getExpiryDate())) {
                log.info("Deleting RefreshToken with id: {}", refreshToken.getId());
                refreshTokenRepository.deleteById(refreshToken.getId());
            }
        });
    }
}