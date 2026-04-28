package com.tmk.core.port.out.persistence;

import com.tmk.core.user.entity.UserAccount;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserAccountPort {

    UserAccount save(UserAccount userAccount);

    Optional<UserAccount> findById(Long userId);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteInactiveUsersBefore(OffsetDateTime threshold);
}
