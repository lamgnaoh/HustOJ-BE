package com.lamgnoah.hustoj.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  @Scheduled(fixedRate = 10000)
  public void computeContestRank() throws JsonProcessingException {
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
          Integer submitCount = rankingUser.getSubmitCount();
          long zscore = (long) ((100-acCount) * Math.ceil(contestDuration/10.0) * 100 + time/1000);
          redisTemplate.opsForZSet().add("contest:" + contest.getId(),
              String.valueOf(rankingUser.getUser().getId()),
              zscore);
//          store user ranking information
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "acceptCount", String.valueOf(acCount));
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "submitCount", String.valueOf(submitCount));
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "time", String.valueOf(time));
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "submission_info", objectMapper.writeValueAsString(rankingUser.getSubmissionInfo()));
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "username", rankingUser.getUser().getUsername());

        }
      } else {
        for (RankingUser rankingUser : rankingUserList) {
          Integer score = rankingUser.getScore();
          redisTemplate.opsForZSet().add("contest:" + contest.getId(),
              String.valueOf(rankingUser.getUser().getId()),
              score);
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "score", String.valueOf(rankingUser.getScore()));
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "submission_info", objectMapper.writeValueAsString(rankingUser.getSubmissionInfo()));
          redisTemplate.opsForHash()
              .put("contest:" + contest.getId() + ":user:" + rankingUser.getUser().getId(),
                  "username", rankingUser.getUser().getUsername());
        }
      }
    }
  }
}
