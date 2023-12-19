package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthorityRepository extends JpaRepository<Authority, Long>,
    JpaSpecificationExecutor<Authority> {
  Authority findByName(AuthorityName authorityName);


}
