package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.domain.enums.ProblemPermission;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.Authority;
import com.lamgnoah.hustoj.entity.Token;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.service.AuthenticationService;
import com.lamgnoah.hustoj.repository.AuthorityRepository;
import com.lamgnoah.hustoj.service.EmailService;
import com.lamgnoah.hustoj.service.JwtUserDetailsService;
import com.lamgnoah.hustoj.service.TokenService;
import com.lamgnoah.hustoj.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUserDetailsService jwtUserDetailsService;
  private final JwtTokenUtil jwtTokenUtil;
  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;
  private final TokenService tokenService;
  private final EmailService emailService;
  @Value("${application.frontend.default-url}")
  private String defaultUrl;



  @Override
  public String login(String username, String password) throws AppException{
    UsernamePasswordAuthenticationToken upToken =
        new UsernamePasswordAuthenticationToken(username, password);
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(upToken);
    } catch (DisabledException e) {
      throw new AppException(ErrorCode.USER_DISABLED);
    } catch (BadCredentialsException e) {
      throw new AppException(ErrorCode.BAD_CREDENTIALS);
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
    return jwtTokenUtil.generateToken(userDetails);
  }

  @Override
  @Transactional
  public User register(UserDTO userDTO , HttpServletRequest request) {
    String username = userDTO.getUsername();
    String email = userDTO.getEmail();
    if (userRepository.existsByUsername(username)) {
      throw new AppException(ErrorCode.DUPLICATED_USERNAME);
    }

    if (userRepository.existsByEmail(email)) {
      throw new AppException(ErrorCode.DUPLICATED_EMAIL);
    }

    List<Authority> authorities = new ArrayList<>();
    authorities.add(authorityRepository.findByName(AuthorityName.ROLE_USER));

    User user = new User(
            username,
            userDTO.getPassword(),
            email,
            userDTO.getFirstname(),
            userDTO.getLastname(),
            authorities);
    user.setEnabled(false);
    user.setProblemPermission(ProblemPermission.NONE);
    String token = UUID.randomUUID().toString();
    User newUser = userRepository.save(user);
    Token confirmationToken = new Token(token , LocalDateTime.now().plusMinutes(15) , user);
    String url = request.getHeader("Origin") != null ? request.getHeader("Origin") : defaultUrl ;
    String verifyPath = url + "/verify/" + token;
    tokenService.saveToken(confirmationToken);
    emailService.send(user.getEmail(), emailService.buildEmail(verifyPath , "registration-verify"));
    return newUser;
  }

  @Override
  public String verifyToken(String verifyToken) {
    Token token  = tokenService.getToken(verifyToken).orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
    if (token.getConfirmedAt() != null) {
      throw new AppException(ErrorCode.EMAIL_ALREADY_CONFIRMED);
    }

    LocalDateTime expiredAt = token.getExpiresAt();
    if (expiredAt.isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }
    tokenService.setConfirmedAt(verifyToken);
    jwtUserDetailsService.enableUser(token.getUser().getEmail());
    return "verified";
  }
}
