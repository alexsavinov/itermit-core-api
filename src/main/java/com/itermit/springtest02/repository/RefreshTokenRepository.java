package com.itermit.springtest02.repository;

import com.itermit.springtest02.model.entity.RefreshToken;
import com.itermit.springtest02.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUserId(Long id);

    @Modifying
    int deleteAllByUserId(Long id);
}
