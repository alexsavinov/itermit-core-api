package com.itermit.springtest02.repository;

import com.itermit.springtest02.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    Optional<User> findFirstByName(String name);

    Optional<User> findByUsername(String name);

//    Optional<User> findByEmail(String name);

    Boolean existsByUsername(String username);

//    Boolean existsByEmail(String email);
}