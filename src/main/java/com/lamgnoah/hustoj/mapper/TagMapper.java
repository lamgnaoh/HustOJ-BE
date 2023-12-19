package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.TagDTO;
import com.lamgnoah.hustoj.entity.Tag;
import java.util.List;
import java.util.Set;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {
  TagDTO entityToDTO(Tag tag);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Tag dtoToEntity(TagDTO tagDTO);

  Set<TagDTO> toTagDTOs(Set<Tag> tags);
  List<TagDTO> toTagDTOs(List<Tag> tags);


  @InheritInverseConfiguration
  Set<Tag> toTags(Set<TagDTO> tagDTOs);


}
