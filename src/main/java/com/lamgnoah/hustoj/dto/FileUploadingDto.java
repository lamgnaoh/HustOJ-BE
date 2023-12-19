package com.lamgnoah.hustoj.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadingDto {
  private String id ;
  private List<Map<String, Object>> info;
  private Boolean spj;
}
