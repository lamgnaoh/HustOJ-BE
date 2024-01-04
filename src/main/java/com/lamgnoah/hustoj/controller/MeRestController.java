package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.dto.ChangePasswordDTO;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.security.JwtUserFactory;
import com.lamgnoah.hustoj.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeRestController {

  private final UserService userService;

  @GetMapping
  public JwtUser getAuthenticatedUser() {
    return JwtUserFactory.create(UserContext.getCurrentUser());
  }

  @PostMapping("/changePassword")
  public ResponseEntity<Boolean> changePassword(@AuthenticationPrincipal JwtUser jwtUser,
                                                @RequestBody ChangePasswordDTO changePasswordDTO) {
    Long userId = jwtUser.getId();
    return ResponseEntity.ok(userService.changePassword(userId, changePasswordDTO));
  }

}
