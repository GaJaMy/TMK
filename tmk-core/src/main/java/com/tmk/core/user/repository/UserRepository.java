package com.tmk.core.user.repository;

import com.tmk.core.user.entity.Provider;
import com.tmk.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByEmail(String email);
}
