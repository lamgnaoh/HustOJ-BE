package com.lamgnoah.hustoj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionDTO implements Serializable {

  private static final long serialVersionUID = -1L;

  private Long id;

  private Long authorId;

  private String authorName;

  private Long problemId;

  private String problemTitle;

  private Long contestId;

  @NotNull(message = "source code cannot be empty")
  private String code;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
  private LocalDateTime createDate = LocalDateTime.now();

  @NotNull(message = "Language type cannot be empty")
  private String language;

  private Integer duration;
  private Integer memory;
  private String result;
  private String resultDetail; // statistic_info
  private Double memoryPercentile;
  private Double durationPercentile;

}
