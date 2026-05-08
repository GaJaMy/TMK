package com.tmk.infra.jpa.repository;

import com.tmk.core.user.entity.UserAccount;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountJpaRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByActiveFalseAndUpdatedAtBefore(OffsetDateTime threshold);
}
