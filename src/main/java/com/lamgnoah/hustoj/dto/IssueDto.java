package com.lamgnoah.hustoj.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueDto {
    protected LocalDateTime createDate;
    private Long id;
    private Long authorId;
    private String authorName;
    private String name;
    private String description;
    private Long problemId;
}
