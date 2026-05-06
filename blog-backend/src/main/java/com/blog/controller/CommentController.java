package com.blog.controller;

import com.blog.entity.Comment;
import com.blog.service.CommentService;
import com.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> getComments(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentTree(articleId));
    }

    @PostMapping
    public Result<Void> addComment(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = requireUserId(auth);
        Long articleId = Long.valueOf(body.get("articleId").toString());
        String content = body.get("content").toString();
        Long parentId = body.containsKey("parentId") && body.get("parentId") != null
                ? Long.valueOf(body.get("parentId").toString()) : null;
        commentService.addComment(userId, articleId, content, parentId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(Authentication auth, @PathVariable Long id) {
        Long userId = requireUserId(auth);
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new AuthenticationCredentialsNotFoundException("请先登录");
        }
        return userId;
    }
}
