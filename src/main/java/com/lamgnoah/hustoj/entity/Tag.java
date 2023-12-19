package com.lamgnoah.hustoj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "tag")
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Tag extends BaseEntity {

  @Column(unique = true, length = 50)
  private String name;

  private Long problemCount;

  @ManyToMany(mappedBy = "tagList", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @NotFound(action = NotFoundAction.IGNORE)
  @ToString.Exclude
  private List<Problem> problemList;

  public Tag() {
    this.problemCount = 0L;
    this.problemList = new ArrayList<>();
  }
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof Tag)) {
      return false;
    }

    Tag tag = (Tag) o;
    return tag.name.equals(name);
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + name.hashCode();
    return code;
  }

//  public void removeProblem(Problem problem) {
//    this.problemList.remove(problem);
//    problem.getTagList().remove(this);
//  }


}
