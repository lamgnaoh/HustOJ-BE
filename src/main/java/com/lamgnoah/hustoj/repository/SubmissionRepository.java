package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.domain.enums.Result;
import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.Submission;
import com.lamgnoah.hustoj.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubmissionRepository extends JpaRepository<Submission , Long> ,
    JpaSpecificationExecutor<Submission> {

  @Modifying
  @Query(value = "delete from submission where problem_id = :id" , nativeQuery = true)
  void deleteByProblemId(Long id);

  void deleteAllByContest(Contest contest);

  List<Submission> findByProblemAndIsPracticeAndAuthor(Problem problem, boolean b, User user);

  List<Submission> findByContestAndProblemAndAuthor(Contest contest, Problem problem, User user);

  List<Submission> findByContest(Contest contest, Pageable pageable);

  long countByContest(Contest contest);

  Optional<Submission> findByContestAndProblem(Contest contest, Problem problem);

  void deleteAllByAuthor(User user);

  List<Submission> findByProblemAndResult(Problem problem, Result result );
}
