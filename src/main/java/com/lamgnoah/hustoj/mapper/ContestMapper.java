package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.ContestDTO;
import com.lamgnoah.hustoj.entity.Contest;
import java.util.List;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContestMapper {
  @InheritInverseConfiguration
  @Mapping(target = "enable", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Contest dtoToEntity(ContestDTO contestDTO);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  @Mapping(target = "password", ignore = true)
  ContestDTO entityToDTO(Contest contest);

  List<ContestDTO> toContestDTOs(List<Contest> contests);

}
