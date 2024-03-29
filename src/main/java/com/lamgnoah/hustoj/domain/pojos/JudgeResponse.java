package com.lamgnoah.hustoj.domain.pojos;

import lombok.Data;

@Data
public class JudgeResponse {
  private Integer cpu_time;
  private Integer result;
  private Integer memory;
  private Integer real_time;
  private Integer singal;
  private Integer error;
  private Integer exit_code;
  private String output_md5;
  private Integer test_case;
  private String output;
}
