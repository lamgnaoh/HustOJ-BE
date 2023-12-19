package com.lamgnoah.hustoj.query;

import java.util.List;
import lombok.Data;

@Data
public class UserQuery {

  private Long id;
  private String username;
  private String name;
  private List<String> role;
}
