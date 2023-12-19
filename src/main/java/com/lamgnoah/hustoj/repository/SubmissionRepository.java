package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.Submission;
import com.lamgnoah.hustoj.entity.User;
import java.util.List;
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
}
