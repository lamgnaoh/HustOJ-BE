package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.AnnouncementDTO;
import com.lamgnoah.hustoj.entity.Announcement;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {
  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  AnnouncementDTO entityToDTO(Announcement announcement);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Announcement dtoToEntity(AnnouncementDTO announcementDTO);
}
