package com.tmk.core.port.out.persistence;

import com.tmk.core.user.entity.Provider;
import com.tmk.core.user.entity.User;

import java.util.Optional;

public interface UserPort {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByEmail(String email);

    User save(User user);
}
