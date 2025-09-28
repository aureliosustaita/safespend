package com.safespend.config;

import com.safespend.domain.AppUser;
import com.safespend.repo.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserBootstrap {
    @Bean
    CommandLineRunner seedUsers(AppUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            repo.findByUsername("user").orElseGet(() ->
                repo.save(new AppUser("user", encoder.encode("user123"), "ROLE_USER")));
            repo.findByUsername("admin").orElseGet(() ->
                repo.save(new AppUser("admin", encoder.encode("admin123"), "ROLE_ADMIN,ROLE_USER")));
        };
    }
}
