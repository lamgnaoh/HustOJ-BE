package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.query.UserQuery;
import com.lamgnoah.hustoj.security.JwtUser;

public interface UserService {


  User create(UserDTO userDTO);

  PageDTO<JwtUser> getAllUsers(Integer page, Integer size, UserQuery userQuery);
}
