package com.lamgnoah.hustoj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Token extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiresAt;
  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @ManyToOne
  @JoinColumn(nullable = false,
      name = "user_id")
  private User user;

  public Token(String token, LocalDateTime expiresAt, User user) {
    this.token = token;
    this.expiresAt = expiresAt;
    this.user = user;
  }


}
