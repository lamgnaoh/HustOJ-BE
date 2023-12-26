package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  UserMapper {

  @InheritInverseConfiguration
  UserDTO entityToDTO(User user);
}
