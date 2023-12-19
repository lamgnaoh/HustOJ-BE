package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.security.JwtUserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username);
    if(user == null) {
      throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
    }
    return JwtUserFactory.create(user);
  }
}
