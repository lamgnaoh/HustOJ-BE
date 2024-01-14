package com.lamgnoah.hustoj.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
  private String newPassword;
  private String confirmPassword;
}
