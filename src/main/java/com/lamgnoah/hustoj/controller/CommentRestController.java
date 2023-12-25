package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.dto.CommentDTO;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping("/page")
    public Page<CommentDTO> getPage(@RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam Long problemId) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "created_at");
        return commentService.pageComment(problemId, pageable);
    }

    @PostMapping("/save")
    public ResponseEntity<CommentDTO> saveComment(@AuthenticationPrincipal JwtUser user,
                                                  @RequestBody CommentDTO commentDTO) {
        commentDTO.setAuthorId(user.getId());
        return ResponseEntity.ok(commentService.saveComment(commentDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }
}
