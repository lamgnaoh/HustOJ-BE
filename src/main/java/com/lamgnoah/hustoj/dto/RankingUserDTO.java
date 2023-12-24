package com.lamgnoah.hustoj.dto;

import lombok.Data;

@Data
public class RankingUserDTO {

  private Long id;

  private Long userId;

  private String userName;

  private Integer acceptCount;

  private Integer submitCount;

  private Integer errorCount;

  private Long time;

  private Integer score;

  private Boolean ranked;

  private Integer rankingNumber;

  private String submissionInfo;
}

