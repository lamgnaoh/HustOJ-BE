package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.entity.Token;
import java.util.Optional;

public interface TokenService {
  void saveToken(Token token);

  Optional<Token> getToken(String token);

  void setConfirmedAt(String verifyToken);
}
