package com.anjali.manufacturing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.anjali.manufacturing.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}