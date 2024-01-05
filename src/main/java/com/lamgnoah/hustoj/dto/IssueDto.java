package com.lamgnoah.hustoj.dto;

import com.lamgnoah.hustoj.domain.enums.IssueStatus;
import com.lamgnoah.hustoj.entity.Issue;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueDto {
    private Long id;
    private String name;
    private String description;
  private IssueStatus status;
  private LocalDateTime createDate;
  private LocalDateTime modifiedDate;
  private Long authorId;
  private String authorName;
    private Long problemId;
  private String problemTitle;

  public static IssueDto fromEntity(Issue issue) {
    IssueDto issueDto = new IssueDto();
    issueDto.setId(issue.getId());
    issueDto.setName(issue.getName());
    issueDto.setDescription(issue.getDescription());
    issueDto.setStatus(issue.getStatus());
    issueDto.setCreateDate(issue.getCreateDate());
    issueDto.setModifiedDate(issue.getModifiedDate());
    issueDto.setAuthorId(issue.getAuthorId());
    issueDto.setProblemId(issue.getProblemId());
    return issueDto;
  }
}
