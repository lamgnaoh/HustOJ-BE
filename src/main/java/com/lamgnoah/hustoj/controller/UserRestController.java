package com.lamgnoah.hustoj.controller;


import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.UserQuery;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.service.UserService;
import java.util.List;
import jdk.jshell.spi.ExecutionControl.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.stylesheets.LinkStyle;

@RestController
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

  private final UserService userService;

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public UserDTO create(@RequestBody UserDTO userDTO) throws AppException {
    return userService.create(userDTO);
  }

  @GetMapping
  public PageDTO<UserDTO> getUsers(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      UserQuery userQuery) {
    return userService.getAllUsers(page, size, userQuery);
  }

  @GetMapping(value = "/{id}")
  public UserDTO getUser(@PathVariable Long id) throws AppException {
    return userService.getUser(id);
  }

  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public UserDTO update(@RequestBody UserDTO userDTO, @PathVariable Long id)
      throws AppException {
    userDTO.setId(id);
    return userService.update(userDTO);
  }
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) throws AppException {
    userService.delete(id);
    return ResponseEntity.ok().build();
  }

}
