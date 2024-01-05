package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.domain.enums.IssueStatus;
import com.lamgnoah.hustoj.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>,
        JpaSpecificationExecutor<Issue> {
  @Query("select i from Issue i " +
      "where (?1 is null or i.status = ?1)")
  Page<Issue> findByStatus(IssueStatus status, Pageable pageable);
}
