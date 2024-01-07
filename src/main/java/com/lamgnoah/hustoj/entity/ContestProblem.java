package com.lamgnoah.hustoj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contest_problem")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContestProblem extends BaseEntity{

  @ManyToOne
  @JoinColumn(name = "contest_id")
  private Contest contest;

  @ManyToOne
  @JoinColumn(name = "problem_id")
  private Problem problem;

  private Integer acceptCount = 0;

  private Integer submitCount = 0;

  private Double acceptRate = 0.0;

  @Column(name = "visible")
  private Boolean visible = true;

  @Version
  private Long version;

}
