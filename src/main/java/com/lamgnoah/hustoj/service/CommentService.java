package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    Page<CommentDTO> pageComment(Long problemId, Pageable pageable);

    CommentDTO saveComment(CommentDTO comment);

    void deleteComment(Long id);

}
