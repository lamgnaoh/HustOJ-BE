package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.CommentDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    PageDTO<CommentDTO> pageComment(Long problemId, Pageable pageable);

    CommentDTO saveComment(CommentDTO comment);

    void deleteComment(Long id);

}
