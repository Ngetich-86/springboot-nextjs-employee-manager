package com.pesira.backend.repository;

import com.pesira.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    java.util.Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(com.pesira.backend.enums.Role role);

    @Query("""
            SELECT u FROM User u
            WHERE :search IS NULL
               OR :search = ''
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
}
