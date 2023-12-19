package com.lamgnoah.hustoj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "ranking_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingUser extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
  @ManyToOne
  @JoinColumn(name = "contest_id")
  @NotFound(action = NotFoundAction.IGNORE)
  private Contest contest;

  private Integer acceptCount = 0;

  private Integer submitCount = 0;

  @Column(name = "submission_info", columnDefinition = "json default ('{}')")
  private String submissionInfo;

  private Double score = 0.0;

  private Long time = 0L;

  private Boolean ranked = true;

  private Integer rankingNumber;

  public void increaseAcceptCount() {
    this.acceptCount++;
  }

  public void increaseSubmitCount() {
    this.submitCount++;
  }

  public void addTime(Long milliseconds) {
    this.time += milliseconds;
  }

  public void addScore(Double score) {
    this.score += score;
  }

}
