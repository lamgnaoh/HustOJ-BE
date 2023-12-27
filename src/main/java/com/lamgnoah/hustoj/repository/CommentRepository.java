package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "select c.* " +
            "from comment c " +
            "         inner join problem p on c.problem_id = p.id " +
            "         inner join user u on c.user_id = u.id " +
        "where problem_id = ?1 and c.parent_comment_id is null",
            countQuery = "select count(*) " +
                    "from comment c " +
                    "         inner join problem p on c.problem_id = p.id " +
                    "         inner join user u on c.user_id = u.id " +
                "where c.problem_id = ?1 and c.parent_comment_id is null",
            nativeQuery = true)
    Page<Comment> findCommentByProblemId(Long problemId, Pageable pageable);


    List<Comment> findByParentCommentIdIn(List<Long> parentCommentIds);
}
