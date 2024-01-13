package com.lamgnoah.hustoj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(name = "problem")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Problem extends BaseEntity {

  @Column(name = "problem_code")
  private String problemCode;

  @Column(name = "title")
  private String title ;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "time_limit")
  private Integer timeLimit;

  @Column (name = "ram_limit")
  private Integer ramLimit;

  @Column(name = "input_description" , length = 500)
  private String inputDescription;

  @Column(name = "output_description" , length = 500)
  private String outputDescription;

  @Column(name = "submit_count")
  private Integer submitCount = 0;

  @Column(name = "accept_count")
  private Integer acceptCount = 0;

  @Column(name = "accept_rate")
  private Double acceptRate = 0.0;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;

  @Column(name = "hint")
  private String hint;

  @Column(name = "sampleio")
  private String sampleIO;

  @Column(name = "test_case_score", length=4000)
  private String testCaseScore;

  @Column(name = "test_case_id")
  private String testCaseId;

  @Column(name = "total_score")
  private Integer totalScore; // for problem type OI

  @Column(name = "visible")
  private Boolean visible;

  @Column(name = "rule_type")
  @Enumerated(EnumType.STRING)
  private ContestRuleType ruleType = ContestRuleType.ACM;


  @Column(name = "special_judged")
  private Boolean specialJudged = false;


  @Fetch(FetchMode.SELECT)
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "problem_tag",
      joinColumns = {@JoinColumn(name = "problem_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")})
  @JsonIgnore
  private Set<Tag> tagList = new HashSet<>();

  @OneToMany(mappedBy = "problem")
  @JsonIgnore
  private List<Submission> submissionList;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @OneToMany(mappedBy = "problem")
  @JsonIgnore
  private List<ContestProblem> contestProblemList;

  @Column(name = "create_in_contest") // for problem which create in contest
  private Boolean createInContest = false;

// use for clone entity
  public Problem(Problem problem){
    this.author = problem.author;
    this.problemCode = problem.problemCode;
    this.title = problem.getTitle();
    this.description = problem.getDescription();
    this.timeLimit = problem.getTimeLimit();
    this.ramLimit = problem.getRamLimit();
    this.inputDescription = problem.getInputDescription();
    this.outputDescription = problem.getOutputDescription();
    this.difficulty = problem.getDifficulty();
    this.hint = problem.getHint();
    this.sampleIO = problem.getSampleIO();
    this.testCaseScore = problem.getTestCaseScore();
    this.testCaseId = problem.getTestCaseId();
    this.totalScore = problem.getTotalScore();
    this.visible = problem.getVisible();
    this.ruleType = problem.getRuleType();
    this.tagList.addAll(problem.getTagList());
    this.createInContest = problem.getCreateInContest();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Problem problem = (Problem) o;
    return Objects.equals(problemCode, problem.problemCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(problemCode);
  }
}
