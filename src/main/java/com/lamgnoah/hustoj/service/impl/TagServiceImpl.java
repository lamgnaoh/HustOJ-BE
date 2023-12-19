package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.dto.TagDTO;
import com.lamgnoah.hustoj.entity.Tag;
import com.lamgnoah.hustoj.mapper.TagMapper;
import com.lamgnoah.hustoj.repository.TagRepository;
import com.lamgnoah.hustoj.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepository;
  private final TagMapper tagMapper;

  @Override
  public List<TagDTO> getTags() {
    List<Tag> allTags = tagRepository.findAll();
    return tagMapper.toTagDTOs(allTags);
  }
}
