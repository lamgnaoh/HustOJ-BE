package com.lamgnoah.hustoj.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.ALWAYS)
public class JudgeServerStatusDTO {
  private String data;
  private String error;
}
