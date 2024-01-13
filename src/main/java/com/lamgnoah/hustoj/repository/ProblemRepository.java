package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem,Long>,
    JpaSpecificationExecutor<Problem> {

  @Query(value = "select * from problem where problem.problem_code = :code "
      + "and create_in_contest = false",nativeQuery = true)
  Optional<Problem> findByProblemCode(@Param("code") String code);


  @Query(value = "select * from problem where problem.title = :title "
      + "and create_in_contest = false",nativeQuery = true)
  Optional<Problem> findByTitle(@Param("title") String title);

  Optional<List<Problem>> findAllByAuthor(User user);

  void deleteAllByAuthor(User user);
}

