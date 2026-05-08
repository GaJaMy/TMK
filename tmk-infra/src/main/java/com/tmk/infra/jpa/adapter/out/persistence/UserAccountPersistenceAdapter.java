package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.port.out.persistence.UserAccountPort;
import com.tmk.core.user.entity.UserAccount;
import com.tmk.infra.jpa.repository.UserAccountJpaRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccountPersistenceAdapter implements UserAccountPort {

    private final UserAccountJpaRepository userAccountJpaRepository;

    @Override
    public UserAccount save(UserAccount userAccount) {
        return userAccountJpaRepository.save(userAccount);
    }

    @Override
    public Optional<UserAccount> findById(Long userId) {
        return userAccountJpaRepository.findById(userId);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return userAccountJpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userAccountJpaRepository.existsByUsername(username);
    }

    @Override
    public void deleteInactiveUsersBefore(OffsetDateTime threshold) {
        userAccountJpaRepository.deleteByActiveFalseAndUpdatedAtBefore(threshold);
    }
}
