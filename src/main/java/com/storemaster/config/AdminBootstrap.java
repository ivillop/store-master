package com.storemaster.config;

import com.storemaster.domain.AppUser;
import com.storemaster.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final AppProperties props;
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        var cfg = props.bootstrap();
        if (cfg == null || !StringUtils.hasText(cfg.adminUsername()) || !StringUtils.hasText(cfg.adminPassword())) {
            return;
        }
        if (users.existsByUsername(cfg.adminUsername())) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(cfg.adminUsername());
        admin.setPasswordHash(encoder.encode(cfg.adminPassword()));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        users.save(admin);
    }
}
