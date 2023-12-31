package com.lamgnoah.hustoj.factory;

import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.RankingUser;
import com.lamgnoah.hustoj.entity.User;
import java.util.HashMap;

public class RankingUserFactory {
  private RankingUserFactory() {
  }

  public static RankingUser create(User user, Contest contest) {
    RankingUser rankingUser = new RankingUser();
    rankingUser.setContest(contest);
    rankingUser.setUser(user);
    rankingUser.setScore(0);
    rankingUser.setTime(0L);
    rankingUser.setSubmissionInfo(new HashMap<>());
    rankingUser.setAcceptCount(0);
    rankingUser.setSubmitCount(0);
    return rankingUser;
  }

}
