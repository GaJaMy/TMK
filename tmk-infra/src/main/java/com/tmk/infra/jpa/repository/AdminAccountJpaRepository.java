package com.tmk.infra.jpa.repository;

import com.tmk.core.admin.entity.AdminAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAccountJpaRepository extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AdminAccount> findAllByOrderByCreatedAtDesc();
}
