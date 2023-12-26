package com.lamgnoah.hustoj.domain.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JudgeServerStatus {

  @JsonProperty("judger_version")
  private String judgerVersion;

  private String hostname;

  @JsonProperty("running_task_number")
  private Integer runningTaskNumber;

  @JsonProperty("cpu_core")
  private Integer cpuCore;

  private Double memory;

  private String action;

  private Integer cpu;

  @JsonProperty("service_url")
  private String serviceUrl;
}
