package com.lamgnoah.hustoj.query;

import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.ContestStatus;
import com.lamgnoah.hustoj.domain.enums.ContestType;
import lombok.Data;

@Data
public class ContestQuery {
  private String name;
  private ContestStatus status;
  private ContestType type;
  private ContestRuleType ruleType;
}
