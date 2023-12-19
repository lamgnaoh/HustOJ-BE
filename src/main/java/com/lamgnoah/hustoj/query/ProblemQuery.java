package com.lamgnoah.hustoj.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.Difficulty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemQuery {

  private String problemCode;

  private String title;

  private String tags;

  private Difficulty difficulty;

  private Boolean visible;

  private String keyword;

  private ContestRuleType contestRuleType;

}
