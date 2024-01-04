package com.lamgnoah.hustoj.entity;

import com.lamgnoah.hustoj.domain.enums.IssueStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private String name;

    private String description;

    private IssueStatus status;
}
