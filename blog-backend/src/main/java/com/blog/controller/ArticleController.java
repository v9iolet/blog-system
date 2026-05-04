package com.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.ArticleDTO;
import com.blog.entity.Article;
import com.blog.entity.Message;
import com.blog.service.ArticleService;
import com.blog.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<IPage<Article>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.getArticlePage(page, size, categoryId, tagId, keyword, Article.STATUS_PUBLISHED));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getArticleDetail(id));
    }

    @GetMapping("/mine/{id}")
    public Result<Article> myDetail(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(articleService.getUserArticleDetail(userId, id));
    }

    @GetMapping("/notifications")
    public Result<List<Message>> notifications(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(articleService.getUserNotifications(userId));
    }

    @PutMapping("/notifications/{messageId}/read")
    public Result<Void> markNotificationRead(Authentication auth, @PathVariable Long messageId) {
        Long userId = (Long) auth.getPrincipal();
        articleService.markNotificationRead(userId, messageId);
        return Result.success();
    }

    @PostMapping
    public Result<Long> create(Authentication auth, @Valid @RequestBody ArticleDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(articleService.createArticle(userId, dto));
    }

    @PutMapping
    public Result<Void> update(Authentication auth, @Valid @RequestBody ArticleDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        articleService.updateArticle(userId, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        articleService.deleteArticle(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success();
    }
}
