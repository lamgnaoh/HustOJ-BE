package com.lamgnoah.hustoj.dto;

import java.util.List;
import lombok.Data;

@Data
public class PageDTO<T> {
  private Integer currentPage;

  private Integer currentSize;

  private Long total;

  private List<T> list;

  public PageDTO(Integer currentPage, Integer currentSize, Long total, List<T> list) {
    this.currentPage = currentPage;
    this.currentSize = currentSize;
    this.total = total;
    this.list = list;
  }

}
