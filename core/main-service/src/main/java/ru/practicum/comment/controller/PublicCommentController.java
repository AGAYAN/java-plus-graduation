package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.dto.comment.CommentDto;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<List<CommentDto>> getByEvent(
      @PathVariable Long eventId,
      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
      @RequestParam(defaultValue = "10") @Positive Integer size) {
    List<CommentDto> comments = commentService.getAllEventComments(eventId, from, size);
    return ResponseEntity.ok(comments);
  }
}
