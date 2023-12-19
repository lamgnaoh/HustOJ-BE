package com.lamgnoah.hustoj.dto;

import com.lamgnoah.hustoj.domain.enums.ProblemPermission;
import com.lamgnoah.hustoj.entity.Authority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Collection;
import lombok.Data;

@Data
public class UserDTO {
  private Long id;

  @NotBlank(message = "Username can not be empty")
  private String username;

  @NotBlank(message = "Password can not be empty")
  private String password;

  @Email(message = "Email cannot be empty")
  private String email;

  @NotBlank(message = "First name can not be empty")
  private String firstname;

  @NotBlank(message = "Last name can not be empty")
  private String lastname;

  private Boolean enabled;

  private Collection<Authority> authorities;

  private String problemPermission;


}
