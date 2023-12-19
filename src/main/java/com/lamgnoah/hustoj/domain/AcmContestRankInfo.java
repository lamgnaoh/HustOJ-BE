package com.lamgnoah.hustoj.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AcmContestRankInfo {
  private Map<Long, ContestProblemSubmitInfo> problemSubmitInfo = new HashMap<>();

  @Data
  @Builder
  public static class ContestProblemSubmitInfo {
    private Boolean isAc;
    private Long acTime;
    private Integer errorNumber;
    private Boolean isFirstAc;
  }
}
