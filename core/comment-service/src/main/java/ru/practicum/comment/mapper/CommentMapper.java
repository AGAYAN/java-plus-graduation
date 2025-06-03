package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.comment.CommentDto;

import java.util.List;
import java.util.Objects;

@Slf4j
@UtilityClass
public class CommentMapper {

  public CommentDto mapToCommentDto(final Comment comment) {
    log.debug("Mapping Comment {} to CommentDto.", comment);
    Objects.requireNonNull(comment);
    return new CommentDto()
        .setId(comment.getId())
        .setUserId(comment.getUser())
        .setEventId(comment.getEventId())
        .setContent(comment.getContent())
        .setCreated(comment.getCreated())
        .setInitiator(comment.isInitiator());
  }

  public List<CommentDto> mapToCommentDto(final List<Comment> comments) {
    if (comments == null || comments.isEmpty()) {
      return List.of();
    }
    return comments.stream()
        .map(CommentMapper::mapToCommentDto)
        .toList();
  }

  public Comment mapTo(final CommentDto comment, final Long userId, final Long event) {
    log.debug("Mapping commentDto {} to comment.", comment);
    Objects.requireNonNull(comment);
    return new Comment()
        .setId(comment.getId())
        .setUser(userId)
        .setEventId(event)
        .setContent(comment.getContent());
  }

}
