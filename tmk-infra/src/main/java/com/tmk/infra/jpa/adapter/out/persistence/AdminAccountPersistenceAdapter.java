package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.infra.jpa.repository.AdminAccountJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountPersistenceAdapter implements AdminAccountPort {

    private final AdminAccountJpaRepository adminAccountJpaRepository;

    @Override
    public AdminAccount save(AdminAccount adminAccount) {
        return adminAccountJpaRepository.save(adminAccount);
    }

    @Override
    public Optional<AdminAccount> findById(Long adminId) {
        return adminAccountJpaRepository.findById(adminId);
    }

    @Override
    public Optional<AdminAccount> findByUsername(String username) {
        return adminAccountJpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminAccountJpaRepository.existsByUsername(username);
    }

    @Override
    public List<AdminAccount> findAll() {
        return adminAccountJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void deleteById(Long adminId) {
        adminAccountJpaRepository.deleteById(adminId);
    }
}
