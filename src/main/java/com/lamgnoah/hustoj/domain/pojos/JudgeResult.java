package com.lamgnoah.hustoj.domain.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lamgnoah.hustoj.domain.enums.Result;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeResult {
  private Integer cpuTime;
  private Result result;
  private Integer memory;
  private Integer realTime;
  private String message;
  private Integer totalCount;
  private Integer passedCount;
  private Integer wrongAnswerCount;
  private Integer cpuTimeLimitExceededCount;
  private Integer timeLimitExceededCount;
  private Integer memoryLimitExceededCount;
  private Integer score; // for oi mode
  private Map<Integer,String> debugInfo;
}
