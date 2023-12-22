package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.domain.enums.ContestStatus;
import com.lamgnoah.hustoj.entity.Contest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContestRepository
    extends JpaRepository<Contest, Long>, JpaSpecificationExecutor<Contest> {
  Optional<Contest> findByName(String name);

  List<Contest> findByStatus(ContestStatus contestStatus);
}
