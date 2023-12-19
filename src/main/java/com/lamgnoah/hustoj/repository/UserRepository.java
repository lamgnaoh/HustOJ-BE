package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findUserByUsername(String username);

  boolean existsByUsername(String username);

  User findByUsername(String username);

  boolean existsByEmail(String email);
}
