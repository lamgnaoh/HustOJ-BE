package com.lamgnoah.hustoj.task;

import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.ContestStatus;
import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.RankingUser;
import com.lamgnoah.hustoj.repository.ContestRepository;
import com.lamgnoah.hustoj.repository.RankingUserRepository;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingComputingTask {

  private final ContestRepository contestRepository;

  private final RankingUserRepository rankingUserRepository;

  private final RedisTemplate<String, String> redisTemplate;

  @Scheduled(fixedRate = 10000)
  public void computeContestRank() {
    List<Contest> contestList =
        contestRepository.findByStatus(ContestStatus.PROCESSING);
    for (Contest contest : contestList) {
      Set<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
      if (contest.getContestRuleType().equals(ContestRuleType.ACM)) {
        // store in redis zset
        long contestDuration = Duration.between(contest.getStartDate(), contest.getEndDate())
            .toSeconds();
        for (RankingUser rankingUser : rankingUserList) {
          Long time = rankingUser.getTime();
          Integer acCount = rankingUser.getAcceptCount();
//          because ac count maybe never greater than 100 in 1 contest
          long zscore = (long) ((100 - acCount) * Math.ceil(contestDuration/10.0) * 10 + time);
          redisTemplate.opsForZSet().add("contest:" + contest.getId(),
              String.valueOf(rankingUser.getUser().getId()),
              zscore);
        }
      } else {
        for (RankingUser rankingUser : rankingUserList) {
          Double score = rankingUser.getScore();
          redisTemplate.opsForZSet().add("contest:" + contest.getId(),
              String.valueOf(rankingUser.getUser().getId()),
              score);
        }
      }
    }
  }
}
