package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.ContestProblem;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.query.ContestProblemQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long>,
    JpaSpecificationExecutor<ContestProblem> {

  List<ContestProblem> findByContest(Contest contest);

  List<ContestProblem> findByProblem(Problem problem);

  @Query(value = "select * from problem where id in "
      + "(select problem_id from contest_problem where contest_id = :contestId) "
      + "and problem_code = :problemCode", nativeQuery = true)
  Optional<Problem> findProblemByProblemCode(@Param("contestId") Long contestId,
      @Param("problemCode") String problemCode);

  @Query(value = "select * from problem where id in "
      + "(select problem_id from contest_problem where contest_id = :contestId) "
      + "and title = :problemTitle", nativeQuery = true)
  Optional<Problem> findProblemByProblemTitle(@Param("contestId") Long contestId,
      @Param("problemTitle") String problemTitle);

  @Query(value = "select cp.* from contest_problem cp "
      + "inner join problem p on (cp.problem_id = p.id) " + "where contest_id = :#{#contest.id} "
      + "and (:#{#contestProblemQuery.keyword} is null or "
      + "((lower(p.problem_code) like lower(concat('%', concat(:#{#contestProblemQuery.keyword},'%'))))"
      + "or (lower(p.title) like lower(concat('%', concat(:#{#contestProblemQuery.keyword},'%'))))))", nativeQuery = true)
  List<ContestProblem> findByContestAndParam(@Param("contest") Contest contest,
      @Param("contestProblemQuery") ContestProblemQuery contestProblemQuery);

  Optional<ContestProblem> findByContestAndProblem(Contest contest, Problem problem);

  void deleteAllByContest(Contest contest);

  Optional<ContestProblem> findByContestAndProblemTitle(Contest contest , String title);

  Optional<ContestProblem> findByContestAndProblemProblemCode(Contest contest, String problemCode);

}
