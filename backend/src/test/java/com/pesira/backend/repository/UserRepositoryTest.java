package com.pesira.backend.repository;

import com.pesira.backend.AbstractIntegrationTest;
import com.pesira.backend.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void seedDataContainsDefaultAccounts() {
        assertThat(userRepository.findByEmail("admin@pesira.local")).isPresent();
        assertThat(userRepository.findByEmail("user@pesira.local")).isPresent();
        assertThat(userRepository.countByRole(Role.ADMIN)).isEqualTo(1);
        assertThat(userRepository.countByRole(Role.USER)).isEqualTo(1);
    }
}
