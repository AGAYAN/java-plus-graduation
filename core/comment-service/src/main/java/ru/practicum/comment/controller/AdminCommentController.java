package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.GetCommentsAdminRequest;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminCommentController {

  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<List<CommentDto>> getComments(
      @RequestParam("eventId") @Positive Long eventId,
      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
      @RequestParam(defaultValue = "10") @Positive Integer size) {
    log.info("Request received GET /admin/comments?eventId={}&from={}&size={}", eventId, from,
        size);
    final List<CommentDto> result =
        commentService.getAllEventComments(new GetCommentsAdminRequest(eventId, from, size));
    log.info("Sending event list size {}.", result.size());
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{commentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeComment(@PathVariable("commentId") Long commentId) {
    log.info("Request received Delete /admin/comments/{}", commentId);
    commentService.delete(commentId);
    log.info("Comment was deleted.");
  }

}
