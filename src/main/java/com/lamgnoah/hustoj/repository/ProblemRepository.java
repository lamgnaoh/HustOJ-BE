package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Problem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProblemRepository extends JpaRepository<Problem,Long>,
    JpaSpecificationExecutor<Problem> {

  Optional<Problem> findByProblemCode(String code);

  Optional<Problem> findByTitle(String title);



}

