package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.security.JwtUserFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeRestController {

  @GetMapping
  public JwtUser getAuthenticatedUser() {
    return JwtUserFactory.create(UserContext.getCurrentUser());
  }

}
