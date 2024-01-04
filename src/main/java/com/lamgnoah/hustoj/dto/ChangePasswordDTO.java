package com.lamgnoah.hustoj.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    String oldPassword;
    String newPassword;
}
