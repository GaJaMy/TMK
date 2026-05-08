package com.tmk.core.port.out.persistence;

import com.tmk.core.admin.entity.AdminAccount;
import java.util.List;
import java.util.Optional;

public interface AdminAccountPort {

    AdminAccount save(AdminAccount adminAccount);

    Optional<AdminAccount> findById(Long adminId);

    Optional<AdminAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AdminAccount> findAll();

    void deleteById(Long adminId);
}
