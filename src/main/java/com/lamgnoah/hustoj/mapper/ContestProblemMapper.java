package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.entity.ContestProblem;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContestProblemMapper {

  @Mapping(target="ruleType" , source="problem.ruleType")
  @Mapping(target = "testCaseId" , source = "problem.testCaseId")
  @Mapping(target= "createInContest", source = "problem.createInContest")
  @Mapping(target = "authorName", source = "problem.author.username")
  @Mapping(target = "problemCode" , source = "problem.problemCode")
  @Mapping(target = "authorId", source = "problem.author.id")
  @Mapping(target = "visible", source = "visible") // visible in contest
  @Mapping(target = "title", source = "problem.title")
  @Mapping(target = "description", source = "problem.description")
  @Mapping(target = "timeLimit", source = "problem.timeLimit")
  @Mapping(target = "ramLimit", source = "problem.ramLimit")
  @Mapping(target = "difficulty", source = "problem.difficulty")
  @Mapping(target = "tagList", source = "problem.tagList")
  @Mapping(target = "inputDescription", source = "problem.inputDescription")
  @Mapping(target = "outputDescription", source = "problem.outputDescription")
  @Mapping(target = "sampleIO", source = "problem.sampleIO")
  @Mapping(target = "hint", source = "problem.hint")
  @Mapping(target = "createDate", source = "problem.createDate")
  @Mapping(target = "modifiedDate", source = "problem.modifiedDate")
  @Mapping(target = "id", source = "problem.id")
  ProblemDTO entityToDTO(ContestProblem contestProblem);
  List<ProblemDTO> toContestProblemDTOs(Collection<ContestProblem> contestProblems);


}
