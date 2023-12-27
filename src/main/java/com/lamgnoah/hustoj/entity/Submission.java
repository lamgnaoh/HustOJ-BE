package com.lamgnoah.hustoj.entity;

import com.lamgnoah.hustoj.domain.enums.Language;
import com.lamgnoah.hustoj.domain.enums.Result;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "submission")
public class Submission extends BaseEntity {

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private Problem problem;

  @Column(columnDefinition = "TEXT")
  private String code;

  private Boolean isPractice;

  @Enumerated(value = EnumType.STRING)
  private Language language;

  private Integer memory;

  private Integer duration;

  @Enumerated(EnumType.STRING)
  private Result result;

  @Column(columnDefinition = "TEXT")
  private String resultDetail;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private Contest contest;

}

