package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.*;
import com.blog.mapper.*;
import com.blog.service.ArticleService;
import com.blog.service.CategoryTagService;
import com.blog.service.StatsService;
import com.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final SysLogMapper sysLogMapper;
    private final MessageMapper messageMapper;
    private final CategoryTagService categoryTagService;
    private final StatsService statsService;
    private final ArticleService articleService;

    // ===== 仪表盘 =====
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(statsService.getDashboardStats());
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public Result<IPage<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<User> result = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return Result.success();
    }

    // ===== 文章管理 =====
    @GetMapping("/articles")
    public Result<IPage<Article>> listArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reviewStatus) {
        return Result.success(articleService.getAdminArticlePage(page, size, status, reviewStatus));
    }

    @PutMapping("/articles/{id}/review")
    public Result<Void> reviewArticle(Authentication auth,
                                      @PathVariable Long id,
                                      @RequestBody Map<String, Object> body) {
        Long adminId = requireUserId(auth);
        Integer reviewStatus = body.get("reviewStatus") == null ? null : Integer.parseInt(body.get("reviewStatus").toString());
        String reviewReason = body.get("reviewReason") == null ? null : body.get("reviewReason").toString();
        articleService.reviewArticle(adminId, id, reviewStatus, reviewReason);
        return Result.success();
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleMapper.deleteById(id);
        return Result.success();
    }

    // ===== 分类标签管理 =====
    @PostMapping("/categories")
    public Result<Category> createCategory(@RequestBody Map<String, String> body) {
        return Result.success(categoryTagService.createCategory(body.get("name"), body.get("description")));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryTagService.deleteCategory(id);
        return Result.success();
    }

    @PostMapping("/tags")
    public Result<Tag> createTag(@RequestBody Map<String, String> body) {
        return Result.success(categoryTagService.createTag(body.get("name")));
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        categoryTagService.deleteTag(id);
        return Result.success();
    }

    // ===== 评论管理 =====
    @GetMapping("/comments")
    public Result<IPage<Comment>> listComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt)));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentMapper.deleteById(id);
        return Result.success();
    }

    // ===== 留言管理 =====
    @GetMapping("/messages")
    public Result<IPage<Message>> listMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Message>().orderByDesc(Message::getCreatedAt)));
    }

    @PutMapping("/messages/{id}/reply")
    public Result<Void> replyMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Message msg = messageMapper.selectById(id);
        msg.setReply(body.get("reply"));
        msg.setStatus(1);
        messageMapper.updateById(msg);
        return Result.success();
    }

    // ===== 系统日志 =====
    @GetMapping("/logs")
    public Result<IPage<SysLog>> listLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(sysLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysLog>().orderByDesc(SysLog::getCreatedAt)));
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new AuthenticationCredentialsNotFoundException("请先登录");
        }
        return userId;
    }
}
