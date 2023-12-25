package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.dto.CommentDTO;
import com.lamgnoah.hustoj.entity.Comment;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.mapper.CommentMapper;
import com.lamgnoah.hustoj.repository.CommentRepository;
import com.lamgnoah.hustoj.repository.ProblemRepository;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final CommentMapper commentMapper;

    @Override
    public Page<CommentDTO> pageComment(Long problemId, Pageable pageable) {
        Page<Comment> pageComment = commentRepository.findCommentByProblemId(problemId, pageable);
        return pageComment.map(commentMapper::entityToDTO);
    }

    @Override
    @Transactional
    public CommentDTO saveComment(CommentDTO commentDTO) {
        Comment comment;
        if (commentDTO.getId() == null) {
            User user = userRepository.findById(commentDTO.getAuthorId())
                    .orElseThrow();
            Problem problem = problemRepository.findById(commentDTO.getProblemId()).orElseThrow();
            comment = new Comment();
            comment.setAuthor(user);
            comment.setProblem(problem);
        } else {
            comment = commentRepository.findById(commentDTO.getId()).orElseThrow();
        }
        comment.setContent(commentDTO.getContent());

        return commentMapper.entityToDTO(commentRepository.save(comment));
    }

    @Override
    @Modifying
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

}
