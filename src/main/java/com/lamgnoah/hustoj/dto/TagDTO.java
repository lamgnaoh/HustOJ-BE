package com.lamgnoah.hustoj.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import lombok.Data;

@Data
public class TagDTO {

  private Long id;

  @NotBlank(message = "Tag name cannot be empty")
  private String name;

  private Long problemCount;

  public TagDTO() {
    this.problemCount = 0L;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TagDTO tagDTO = (TagDTO) o;
    return Objects.equals(name, tagDTO.name);
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + name.hashCode();
    return code;
  }
}
