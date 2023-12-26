package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.query.UserQuery;

public interface UserService {

  UserDTO create(UserDTO userDTO);

  PageDTO<UserDTO> getAllUsers(Integer page, Integer size, UserQuery userQuery);

  UserDTO getUser(Long id);

  UserDTO update(UserDTO userDTO);

  void delete(Long id);
}
