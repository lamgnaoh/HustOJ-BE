package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.dto.TagDTO;
import com.lamgnoah.hustoj.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagRestController {

  private final TagService tagService;
  @GetMapping
  public List<TagDTO> getTags() {
    return tagService.getTags();
  }

}
