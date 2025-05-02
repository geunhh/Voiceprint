package com.voiceprint.backend.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthProviderAndEmail(User.AuthProvider provider, String email);
}

