package com.lamgnoah.hustoj.utils;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class NetResult {

  public Integer code;

  public String message;

  public Object data;
}

