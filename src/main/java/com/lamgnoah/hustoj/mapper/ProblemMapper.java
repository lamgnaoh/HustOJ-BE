package com.lamgnoah.hustoj.mapper;



import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.service.ProblemService;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" , uses = ProblemService.class)
public interface ProblemMapper {

  @Mapping(target = "myStatus" , ignore = true)
  @Mapping(target = "totalScore" , source = "totalScore")
  @Mapping(target = "modifiedDate", source = "modifiedDate")
  @Mapping(target = "createDate", source = "createDate")
  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  ProblemDTO entityToDTO(Problem problem);


  List<ProblemDTO> toProblemDTOs(List<Problem> problems);



  @InheritInverseConfiguration
  @BeanMapping(builder = @Builder( disableBuilder = true ))
//  @Mapping(target = "averageAcceptTime", ignore = true)
  @Mapping(target = "createInContest" , ignore = true)
  @Mapping(target = "acceptCount", ignore = true)
  @Mapping(target = "submitCount", ignore = true)
  @Mapping(target = "acceptRate", ignore = true)
  @Mapping(target = "createDate", ignore = true)
//  @Mapping(target = "lastUsedDate", ignore = true)
  @Mapping(target = "modifiedDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Problem dtoToEntity(ProblemDTO problemDTO);
}
