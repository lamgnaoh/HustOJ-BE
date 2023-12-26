package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.RankingUser;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.query.RankingUserQuery;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingUserRepository extends JpaRepository<RankingUser , Long>{
  Optional<RankingUser> findByContestAndUser(Contest contest, User user);

  Set<RankingUser> findByContest(Contest contest);

  @Query(value = "select ru.* from ranking_user ru " 
      + "inner join contest c on ru.contest_id = c.id " 
      + "inner join user u on ru.user_id = u.id "
      + "where contest_id = :#{#contest.id} " 
      + "and (:#{#rankingUserQuery.name} is null or "
      + "((lower(u.username) like lower(concat('%', concat(:#{#rankingUserQuery.name},'%'))))))" ,
      countQuery = "select count(*) from ranking_user ru "
          + "inner join contest c on ru.contest_id = c.id "
          + "inner join user u on ru.user_id = u.id "
          + "where contest_id = :#{#contest.id} "
          + "and (:#{#rankingUserQuery.name} is null or "
          + "((lower(u.username) like lower(concat('%', concat(:#{#rankingUserQuery.name},'%'))))))",
      nativeQuery = true)
  Page<RankingUser> findByContestAndParams(@Param("contest") Contest contest ,
      @Param("rankingUserQuery") RankingUserQuery rankingUserQuery , Pageable pageable);


  List<RankingUser> findByContestOrderByAcceptCountDescTimeAsc(Contest contest);

  List<RankingUser> findByContestOrderByScoreDesc(Contest contest);

  void deleteAllByUser(User user);
}
