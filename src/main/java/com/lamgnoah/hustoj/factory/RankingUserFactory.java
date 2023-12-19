package com.lamgnoah.hustoj.factory;

import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.RankingUser;
import com.lamgnoah.hustoj.entity.User;

public class RankingUserFactory {
  private RankingUserFactory() {
  }

  public static RankingUser create(User user, Contest contest) {
    RankingUser rankingUser = new RankingUser();
    rankingUser.setContest(contest);
    rankingUser.setUser(user);
//    Todo
// process exclude rankingUser
//    if (!contest.getUserListExcluded().contains(user)) {
//      rankingUser.setRanked(true);
//    } else {
//      rankingUser.setRanked(false);
//    }
    return rankingUser;
  }

}
