package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.security.JwtAuthenticationRequest;
import com.lamgnoah.hustoj.security.JwtAuthenticationResponse;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.security.JwtUserFactory;
import com.lamgnoah.hustoj.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationRestController {
  private final AuthenticationService authenticationService;

  @PostMapping("/api/v1/auth")
  public ResponseEntity<JwtAuthenticationResponse> createAuthenticationToken(
      @RequestBody JwtAuthenticationRequest authenticationRequest
  ) throws AppException{
    final String token =
        authenticationService.login(
            authenticationRequest.getUsername(), authenticationRequest.getPassword());
    // Return the token
    return ResponseEntity.ok(new JwtAuthenticationResponse(token));
  }

  @PostMapping(value = "/api/v1/register")
  public JwtUser register(@Validated @RequestBody UserDTO userDTO)
      throws AppException {
    User user = authenticationService.register(userDTO);
    return JwtUserFactory.create(user);
  }


}
