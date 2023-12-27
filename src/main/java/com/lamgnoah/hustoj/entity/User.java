package com.lamgnoah.hustoj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.domain.enums.ProblemPermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{

  @Column(unique = true, length = 50)
  @NotBlank(message = "Username can not be empty")
  @Size(min = 4, max = 50)
  private String username;

  @Column(length = 100)
  @NotNull(message = "Password can not be blank")
  private String password;

  @Column(length = 50)
  private String firstname;

  @Column(length = 50)
  private String lastname;

  @Column(length = 50)
  @NotNull(message = "name cannot be empty")
  private String name;


  @Column(unique = true, length = 50)
  @NotNull(message = "E-mail can not be empty")
  @Email(message = "Email format error")
  private String email;

  @Column(length = 50)
  private Long acCount = 0L;

  @Column(length = 50)
  private Long submitCount = 0L;

  @Column(length = 50)
  private Double acRate = 0.0;

  @Column(name = "total_score")
  private Integer totalScore = 0;

//  example:
/*
*
* {
        "problems": {
            "1": {
                "status": JudgeStatus.ACCEPTED,
                "id": "1000"
            }
        },
        "contest_problems": {
            "1": {
                "status": JudgeStatus.ACCEPTED,
                "id": "1000"
            }
        }
    }
*
*
* */
  @Column(name = "acm_problems_status" ,columnDefinition = "json default ('{}')" )
  private String acmProblemsStatus = "{}";

  @Column(name = "oi_problems_status" , columnDefinition = "json default ('{}')")
  private String oiProblemsStatus = "{}";

  @NotNull
  private Boolean enabled = true;

  @Column(name = "last_password_reset_date")
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date lastPasswordResetDate =new Date();

  @ManyToMany(fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @JoinTable(
      name = "user_authority",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "authority_id", referencedColumnName = "id")})
  @JsonIgnore
  private List<Authority> authorities = new ArrayList<>();

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Problem> problemList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Contest> contestList;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<RankingUser> rankingUserList;

  @Column(name = "problem_permission")
  @Enumerated(EnumType.STRING)
  private ProblemPermission problemPermission = ProblemPermission.NONE;

  public User(
      @NotNull @Size(min = 4, max = 50) String username,
      @NotNull String password,
      @NotNull String email,
      String firstname,
      String lastname,
      List<Authority> authorities) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.username = username;
    this.password = encoder.encode(password);
    this.email = email;
    this.firstname = firstname;
    this.lastname = lastname;
    this.name = firstname + " " + lastname;
    this.authorities = authorities;
  }


  public boolean isAdmin() {
    for (Authority authority : authorities) {
      if (AuthorityName.ROLE_SUPER_ADMIN.equals(authority.getName()) || AuthorityName.ROLE_ADMIN.equals(authority.getName())) {
        return true;
      }
    }
    return false;
  }

  public boolean canManageAllProblem(){
    return problemPermission.equals(ProblemPermission.ALL);
  }


  public void setPassword(String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.password = encoder.encode(password);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof User)) {
      return false;
    }

    User user = (User) o;
    return user.getId().equals(this.getId());
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + name.hashCode();
    return code;
  }

  public void addScore(Integer lastTimeScore, Integer thisTimeScore) {
    this.totalScore = this.totalScore - lastTimeScore + thisTimeScore;
  }
}
