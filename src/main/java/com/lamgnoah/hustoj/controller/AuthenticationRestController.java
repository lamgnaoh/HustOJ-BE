package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.dto.RecoverPasswordDTO;
import com.lamgnoah.hustoj.dto.ResetPasswordDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.security.JwtAuthenticationRequest;
import com.lamgnoah.hustoj.security.JwtAuthenticationResponse;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.security.JwtUserFactory;
import com.lamgnoah.hustoj.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
  public JwtUser register(@Validated @RequestBody UserDTO userDTO , HttpServletRequest request)
      throws AppException {
    User user = authenticationService.register(userDTO , request);
    return JwtUserFactory.create(user);
  }

  @GetMapping(value = "/api/v1/verify")
  public ResponseEntity<String> verifyToken(@RequestParam("token") String token) {
    return ResponseEntity.ok(authenticationService.verifyToken(token));
  }

  @PostMapping(value = "/api/v1/forgot-password")
  public ResponseEntity<String> forgotPassword(@RequestBody RecoverPasswordDTO recoverPasswordDTO , HttpServletRequest request){
    return ResponseEntity.ok(authenticationService.forgotPassword(recoverPasswordDTO , request));
  }

  @PostMapping(value = "/api/v1/reset-password")
  public ResponseEntity<Void> verifyResetPassword(@RequestParam("token") String token , @RequestBody ResetPasswordDTO resetPasswordDTO , HttpServletRequest request){
    authenticationService.resetPassword(token , resetPasswordDTO);
    return ResponseEntity.ok().build();
  }

}
