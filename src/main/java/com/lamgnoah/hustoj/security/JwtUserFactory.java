package com.lamgnoah.hustoj.security;

import com.lamgnoah.hustoj.entity.Authority;
import com.lamgnoah.hustoj.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtUserFactory {

  private JwtUserFactory(){}


  public static JwtUser create(User user) {
    return new JwtUser(
        user.getId(),
        user.getName(),
        user.getUsername(),
        user.getPassword(),
        user.getFirstname(),
        user.getLastname(),
        user.getEmail(),
        user.getAcCount(),
        user.getSubmitCount(),
        user.getTotalScore(),
        user.getAcRate(),
        user.getEnabled(),
        user.getLastPasswordResetDate(),
        mapToGrantedAuthorities(user.getAuthorities()));
  }

  public static List<JwtUser> createList(Collection<User> userList) {
    List<JwtUser> jwtUserList = new ArrayList<>();
    Iterator<User> userIterator = userList.iterator();
    while (userIterator.hasNext()) {
      User user = userIterator.next();
      jwtUserList.add(
          JwtUserFactory.create(user)
      );
    }
    return jwtUserList;
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
    return authorities.stream()
        .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
        .collect(Collectors.toList());
  }

}
