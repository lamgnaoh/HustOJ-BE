package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.entity.Token;
import com.lamgnoah.hustoj.repository.TokenRepository;
import com.lamgnoah.hustoj.service.TokenService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
  private final TokenRepository tokenRepository;

  @Override
  public void saveToken(Token token) {
    tokenRepository.save(token);
  }

  @Override
  public Optional<Token> getToken(String token) {
    return tokenRepository.findByToken(token);
  }

  @Override
  public void setConfirmedAt(String verifyToken) {
    tokenRepository.updateConfirmedAt(verifyToken, LocalDateTime.now());
  }
}
