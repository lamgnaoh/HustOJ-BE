package com.lamgnoah.hustoj.entity;

import com.lamgnoah.hustoj.domain.enums.IssueStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "issue")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Issue extends BaseEntity {
    private String name;
    private String description;
  @Enumerated(EnumType.STRING)
    private IssueStatus status;
  private Long authorId;
  private Long problemId;
}
