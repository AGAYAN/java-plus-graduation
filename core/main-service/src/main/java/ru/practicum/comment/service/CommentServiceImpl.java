package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.controller.EventController;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.GetCommentsAdminRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;


import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final EventController eventStates;

  //private final UserRepository userRepository;
  //private final EventRepository eventRepository;

  /**
   * /users/{userId}/comments?eventId={eventId}
   * <p> Saves a new comment data initiated by a current user.
   */
  @Override
  public CommentDto addComment(final CommentDto commentDto) {
    Long userId = commentDto.getUserId();
    Long eventId = commentDto.getEventId();

    // Предполагается, что для проверки состояния нужно использовать объект Event
    EventFullDto event = eventStates.getEventById(commentDto.getEventId(), null);

    // Проверка на состояние публикации события
    if (!event.getState().equals("PUBLISHED")) {
      log.warn("Cannot add comment to an unpublished event, event state = {}", event.getState());
      throw new ConflictException("Cannot save comments for an unpublished event.");
    }

    // Маппинг комментария с учетом userId и eventId
    Comment comment = CommentMapper.mapTo(commentDto, userId, eventId);
    comment.setCreated(LocalDateTime.now());

    // Сравнение по ID инициатора (если текущий пользователь является инициатором события)
    if (userId.equals(event.getInitiator())) {
      comment.setInitiator(true);
    }

    Comment savedComment = commentRepository.save(comment);
    return CommentMapper.mapToCommentDto(savedComment);
  }

  /**
   * /users/{userId}/comments/{commentId}
   * <p> Deletes current user's comment
   */
  @Override
  public void delete(final Long userId, final Long commentId) {
    Comment comment = fetchComment(commentId);

    if (!comment.getUser().equals(userId)) {
      throw new ConflictException("A user can delete only their own comments.");
    }

    commentRepository.delete(comment);
  }

  /**
   * /admin/comments/{commentId}
   * <p> Deletes specified comment
   */
  @Override
  public void delete(final Long commentId) {
    Comment comment = fetchComment(commentId);
    commentRepository.delete(comment);
  }

  /**
   * /users/{userId}/comments/{commentId}
   * <p> Update current user's comment
   */
  @Override
  public CommentDto updateUserComment(final Long userId, final Long commentId,
                                      final CommentDto commentDto) {
    Comment comment = fetchComment(commentId);
//    fetchUser(userId);

    if (!comment.getUser().equals(userId)) {
      throw new ConflictException("A user can update only their own comments.");
    }

    comment.setContent(commentDto.getContent());
    Comment updated = commentRepository.save(comment);

    return CommentMapper.mapToCommentDto(updated);
  }

  /**
   * /users/{userId}/comments
   * <p> Get all comments created by given user, used for Private API
   */
  @Transactional(readOnly = true)
  @Override
  public List<CommentDto> getAllUserComments(final Long userId) {
    return CommentMapper.mapToCommentDto(commentRepository.findByUser(userId));
  }

  /**
   * /admin/comment
   * <p> Get comments related specified event, used for Admin API
   */
  @Transactional(readOnly = true)
  @Override
  public List<CommentDto> getAllEventComments(final GetCommentsAdminRequest param) {
    final List<Comment> comments =
        getEventComments(param.getEventId(), param.getFrom(), param.getSize());
    return CommentMapper.mapToCommentDto(comments);
  }

  /**
   * /events/{eventId}/comments
   * <p> Get comments of given event for public API
   */
  @Transactional(readOnly = true)
  @Override
  public List<CommentDto> getAllEventComments(final Long eventId, final int from, final int size) {
    return CommentMapper.mapToCommentDto(getEventComments(eventId,from,size));
  }

  private List<Comment> getEventComments(final Long eventId, final int from, final int size) {
    final PageRequest page = PageRequest.of(from / size, size);
    return commentRepository.findAllByEventId(eventId, page).getContent();
  }
//
//  private User fetchUser(final Long userId) {
//    log.debug("Fetching user with ID {}", userId);
//    return userRepository.findById(userId)
//        .orElseThrow(() -> {
//          log.warn("User with ID {} not found.", userId);
//          return new NotFoundException("The user not found.");
//        });
//  }
//
//  private Event fetchEvent(final Long eventId) {
//    return eventRepository.findById(eventId)
//        .orElseThrow(() -> {
//          log.warn("Event with ID {} not found.", eventId);
//          return new NotFoundException("The event not found.");
//        });
//  }

  private Comment fetchComment(final Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> {
          log.warn("Comment with ID {} not found.", commentId);
          return new NotFoundException("The comment not found.");
        });
  }
}
