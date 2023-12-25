package com.lamgnoah.hustoj.mapper;

import com.lamgnoah.hustoj.dto.CommentDTO;
import com.lamgnoah.hustoj.entity.Comment;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @InheritInverseConfiguration

    @Mapping(target = "authorName", source = "author.username")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "problemId", source = "problem.id")
    CommentDTO entityToDTO(Comment comment);
}
