package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.domain.enums.ProblemPermission;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.Authority;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.service.AuthenticationService;
import com.lamgnoah.hustoj.repository.AuthorityRepository;
import com.lamgnoah.hustoj.service.JwtUserDetailsService;
import com.lamgnoah.hustoj.utils.JwtTokenUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUserDetailsService jwtUserDetailsService;
  private final JwtTokenUtil jwtTokenUtil;
  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;


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
  public User register(UserDTO userDTO) {
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
    user.setProblemPermission(ProblemPermission.NONE);
    return userRepository.save(user);
  }
}
