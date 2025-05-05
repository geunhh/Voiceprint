package com.voiceprint.backend.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthProviderAndEmail(User.AuthProvider provider, String email);

    Optional<User> findByEmail(String email);
}

