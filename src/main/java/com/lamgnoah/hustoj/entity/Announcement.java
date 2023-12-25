package com.lamgnoah.hustoj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "announcement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Announcement extends BaseEntity {

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Column(length = 50)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

}
