package com.lamgnoah.hustoj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lamgnoah.hustoj.entity.SampleIO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class ProblemDTO {

  private Long id;

  private Long contestId;

  private Long authorId;
  @NotBlank(message = "Problem code can not blank")
  private String problemCode;

  private String authorName;

  @NotBlank(message = "problem title can not blank")
  private String title;

  @NotBlank(message = "problem description can not blank")
  private String description;

  @NotNull(message = "time limit can not null")
  private Integer timeLimit;

  @NotNull(message = "ram limit can not null")
  private Integer ramLimit;

  @NotNull(message = "difficulty can not null")
  private String difficulty;

  @NotBlank(message = "input description can not blank")
  private String inputDescription;

  @NotBlank(message = "output description can not blank")
  private String outputDescription;

  @NotEmpty(message = "Sample input/output can not empty")
  private List<SampleIO> sampleIOList;

  private String sampleIO;

  @NotNull(message = "Test Data can not null")
  private String testCaseId;

  private String ruleType;

  private List<TestcaseInfoDTO> testcaseInfos;

  private Boolean visible;

  private Set<TagDTO> tagList;

  private String hint;

  private Boolean specialJudged;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
  private LocalDateTime createDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
  private LocalDateTime modifiedDate;

  private Integer acceptCount;

  private Integer submitCount;

  private Double acceptRate;

  private Boolean createInContest;
  private Integer totalScore;
  private String myStatus;
}
