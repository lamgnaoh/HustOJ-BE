package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.RankingUserDTO;
import com.lamgnoah.hustoj.entity.RankingUser;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RankingUserMapper {

  @Mapping(target = "submissionInfo" , ignore = true)
  @Mapping(target = "userName", source = "user.username")
  @Mapping(target = "userId", source = "user.id")
  RankingUserDTO entityToDTO(RankingUser rankingUser);
  List<RankingUserDTO> toRankingUserDTOs(List<RankingUser> rankingUserList);
}
