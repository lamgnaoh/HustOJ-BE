package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findUserByUsername(String username);

  boolean existsByUsername(String username);

  User findByUsername(String username);

  boolean existsByEmail(String email);
  boolean existsByUsernameAndIdIsNot(String username, Long id);


  boolean existsByEmailAndIdIsNot(String email, Long id);

  @Transactional
  @Modifying
  @Query("UPDATE User a " +
      "SET a.enabled = TRUE WHERE a.email = ?1")
  int enableUser(String email);

  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);
}
