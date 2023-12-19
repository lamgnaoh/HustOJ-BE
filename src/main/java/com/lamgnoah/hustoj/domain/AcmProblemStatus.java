package com.lamgnoah.hustoj.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AcmProblemStatus {
  private Map<Long , ProblemStatus> problems = new HashMap<>();
  private Map<Long , ContestProblemStatus> contestProblem = new HashMap<>();

  @Data
  @Builder
  public static class ProblemStatus{
    private Long id;
    private String status;
  }
  @Data
  @Builder
  public static class ContestProblemStatus{
    private Long id;
    private String status;
  }
}
