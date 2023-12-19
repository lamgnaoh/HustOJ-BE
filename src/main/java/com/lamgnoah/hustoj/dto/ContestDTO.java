package com.lamgnoah.hustoj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ContestDTO {

  private Long id;

  private Long authorId;

  private String authorName;

  private String password;

  @NotBlank(message = "Contest name cannot be empty")
  private String name;

  @NotBlank(message = "Contest description cannot be empty")
  private String description;

  private String contestType; // PUBLIC SECRET_WITH_PASSWORD

  private String contestRuleType; // ACM OI

  private String status;


  @NotNull(message = "Start time cannot be null")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime startDate;

  @NotNull(message = "End time cannot be null")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime endDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDate;

  private Boolean enable;

  private Boolean visible;

}
