package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.SubmissionDTO;
import com.lamgnoah.hustoj.entity.Submission;
import java.util.List;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  @Mapping(target = "problemId", source = "problem.id")
  @Mapping(target = "problemTitle", source = "problem.title")
  @Mapping(target = "contestId", source = "contest.id")
  SubmissionDTO entityToDTO(Submission submission);

  @InheritInverseConfiguration
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Submission dtoToEntity(SubmissionDTO submissionDTO);

  List<SubmissionDTO> toSubmissionDTOs(List<Submission> submissionList);
}
