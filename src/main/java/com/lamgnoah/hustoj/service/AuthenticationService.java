package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;

public interface AuthenticationService {

  String login(String username, String password) throws AppException;

  User register(UserDTO userDTO);
}
