package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>,
        JpaSpecificationExecutor<Issue> {
}
