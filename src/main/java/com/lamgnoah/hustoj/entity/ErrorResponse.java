package com.lamgnoah.hustoj.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
  private String code;
  private String message;
  private String url;
}
