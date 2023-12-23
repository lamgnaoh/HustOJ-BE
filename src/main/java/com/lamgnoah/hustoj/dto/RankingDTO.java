package com.lamgnoah.hustoj.dto;

import java.util.List;
import lombok.Data;

@Data
public class RankingDTO {

  private Long contestId;

  private String contestName;

  private List<ProblemDTO> contestProblemList;

  private List<String> rankingUserInfo;
}
