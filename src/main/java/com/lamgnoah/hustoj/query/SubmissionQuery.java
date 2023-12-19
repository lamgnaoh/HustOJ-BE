package com.lamgnoah.hustoj.query;

import com.lamgnoah.hustoj.domain.enums.Language;
import lombok.Data;

@Data
public class SubmissionQuery {

  private String username;

  private Long problemId;

  private Language language;

  private Boolean isPractice;

  private Boolean isPersonal;

}
