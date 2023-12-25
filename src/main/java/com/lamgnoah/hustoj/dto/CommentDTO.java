package com.lamgnoah.hustoj.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    protected LocalDateTime createDate;
    private Long id;
    private Long authorId;
    private String authorName;
    private Long problemId;
    private String content;
}
