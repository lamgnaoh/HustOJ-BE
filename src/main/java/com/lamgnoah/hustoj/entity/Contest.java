package com.lamgnoah.hustoj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.ContestStatus;
import com.lamgnoah.hustoj.domain.enums.ContestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Entity
@Table(name = "contest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contest extends BaseEntity {


  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Column(unique = true, length = 50)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String password;

  @Enumerated(EnumType.STRING)
  private ContestType contestType = ContestType.PUBLIC;

  @Enumerated(EnumType.STRING)
  private ContestRuleType contestRuleType = ContestRuleType.ACM;


  @Enumerated(EnumType.STRING)
  private ContestStatus status = ContestStatus.NOT_STARTED;


  private Boolean realTimeRank = true;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  private Boolean enable = false;
  private Boolean visible = false;

  @Fetch(FetchMode.SUBSELECT)
  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private List<Submission> submissionList;

  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private Set<ContestProblem> contestProblemSet = new HashSet<>();


//  Todo:
//  @JsonIgnore
//  private Set<ContestProblem> contestProblemSet = new HashSet<>();

  public void setPassword(String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.password = encoder.encode(password);
  }
}
