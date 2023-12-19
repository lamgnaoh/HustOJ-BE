package com.lamgnoah.hustoj.controller;


import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.UserQuery;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

  private final UserService userService;

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public User create(@RequestBody UserDTO userDTO) throws AppException {
    return userService.create(userDTO);
  }

  @GetMapping
  public PageDTO<JwtUser> getUser(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      UserQuery userQuery) {
    return userService.getAllUsers(page, size, userQuery);
  }

}
