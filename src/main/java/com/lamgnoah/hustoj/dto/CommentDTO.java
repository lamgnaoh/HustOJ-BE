package com.lamgnoah.hustoj.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentDTO {
    protected LocalDateTime createDate;
    private Long id;
    private Long parentCommentId;
    private Long authorId;
    private String authorName;
    private Long problemId;
    private String content;
    private List<CommentDTO> listSubComment;
}
