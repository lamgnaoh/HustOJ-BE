package com.lamgnoah.hustoj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnnouncementDTO {

  private Long id;

  private String authorId;

  private String authorName;

  @NotBlank(message = "Content cannot be empty")
  private String content;

  @NotBlank(message = "Title cannot be empty")
  private String title;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
  private LocalDateTime modifiedDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
  private LocalDateTime createDate;

}
