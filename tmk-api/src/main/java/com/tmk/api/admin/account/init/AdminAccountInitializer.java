package com.tmk.api.admin.account.init;

import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private static final String INITIAL_ADMIN_USERNAME = "mansa0805";
    private static final String INITIAL_ADMIN_PASSWORD = "wpfkem!@34";

    private final AdminAccountPort adminAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminAccountPort.existsByUsername(INITIAL_ADMIN_USERNAME)) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        AdminAccount initialAdminAccount = AdminAccount.create(
                INITIAL_ADMIN_USERNAME,
                passwordEncoderPort.encode(INITIAL_ADMIN_PASSWORD),
                null,
                now
        );

        try {
            adminAccountPort.save(initialAdminAccount);
            log.info("Initial admin account created: {}", INITIAL_ADMIN_USERNAME);
        } catch (DataIntegrityViolationException e) {
            log.info("Initial admin account already exists: {}", INITIAL_ADMIN_USERNAME);
        }
    }
}
