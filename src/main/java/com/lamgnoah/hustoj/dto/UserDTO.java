package com.lamgnoah.hustoj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lamgnoah.hustoj.entity.Authority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

  private Long acCount ;

  private Long submitCount;

  private Double acRate;

  private String name;

  private Integer totalScore;

  private List<Authority> authorities;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
  private LocalDateTime createDate;

  private String problemPermission;


}
