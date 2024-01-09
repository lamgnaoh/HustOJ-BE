package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

  String login(String username, String password) throws AppException;

  User register(UserDTO userDTO , HttpServletRequest request);

  String verifyToken(String token);
}
