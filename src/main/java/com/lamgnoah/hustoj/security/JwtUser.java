package com.lamgnoah.hustoj.security;

import java.util.Collection;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
public class JwtUser implements UserDetails {
  private final Long id;

  private final String name;

  private final String username;

  private final String password;

  private final String firstname;

  private final String lastname;

  private final String email;

  private final Long acCount;

  private final Long submitCount;

  private final Double acRate;

  private final Boolean enabled;

  private final Date lastPasswordResetDate;

  private final Collection<? extends GrantedAuthority> authorities;


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
//  Todo:
//  @JsonIgnore
//  public Date getLastPasswordResetDate() {
//    return lastPasswordResetDate;
//  }
}
