package com.lamgnoah.hustoj.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class OiProblemStatus {
  private Map<Long , ProblemStatus> problems = new HashMap<>();
  private Map<Long , ContestProblemStatus> contestProblem = new HashMap<>();

  @Data
  @Builder
  public static class ProblemStatus{
    private Long id;
    private String status;
    private Integer score;
  }
  @Data
  @Builder
  public static class ContestProblemStatus{
    private Long id;
    private String status;
    private Integer score;
  }
}