package com.lamgnoah.hustoj.domain.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WADebugInfo {
  private String input;
  private String expectedOutput;
  private String output;
}
