package com.pesira.backend.security;

import com.pesira.backend.AbstractIntegrationTest;
import com.pesira.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncodingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void seededPasswordsMatchExpectedPlaintextValues() {
        var admin = userRepository.findByEmail("admin@example.com").orElseThrow();
        var user = userRepository.findByEmail("user@example.com").orElseThrow();

        assertThat(admin.getPassword()).startsWith("$2");
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("user123", user.getPassword())).isTrue();
    }
}
