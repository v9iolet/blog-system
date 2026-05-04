package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.config.ArticleReviewNotificationWebSocketHandler;
import com.blog.dto.ArticleDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Message;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.MessageMapper;
import com.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final MessageMapper messageMapper;
    private final ArticleReviewNotificationWebSocketHandler notificationWebSocketHandler;

    public IPage<Article> getArticlePage(int page, int size, Long categoryId,
                                         Long tagId, String keyword, Integer status) {
        return getArticlePage(page, size, categoryId, tagId, keyword, status, Article.REVIEW_APPROVED, null);
    }

    public IPage<Article> getAdminArticlePage(int page, int size, Integer status, Integer reviewStatus) {
        return getArticlePage(page, size, null, null, null, status, reviewStatus, null);
    }

    public IPage<Article> getUserArticles(Long userId, int page, int size) {
        return getArticlePage(page, size, null, null, null, null, null, userId);
    }

    public Article getArticleDetail(Long id) {
        Article article = articleMapper.selectArticleDetail(id);
        if (article == null) throw new RuntimeException("文章不存在");
        if (Article.STATUS_PUBLISHED != article.getStatus() || !Integer.valueOf(Article.REVIEW_APPROVED).equals(article.getReviewStatus())) {
            throw new RuntimeException("文章暂不可查看");
        }
        article.setTags(tagMapper.selectByArticleId(id));
        articleMapper.incrementViews(id);
        return article;
    }

    public Article getUserArticleDetail(Long userId, Long id) {
        Article article = articleMapper.selectArticleDetail(id);
        if (article == null) throw new RuntimeException("文章不存在");
        if (!article.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权查看");
        }
        article.setTags(tagMapper.selectByArticleId(id));
        return article;
    }

    @Transactional
    public Long createArticle(Long authorId, ArticleDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setCoverImage(dto.getCoverImage());
        article.setAuthorId(authorId);
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : Article.STATUS_DRAFT);
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        resetReviewFields(article);
        articleMapper.insert(article);
        saveTags(article.getId(), dto.getTagIds());
        if (Article.STATUS_PUBLISHED == article.getStatus()) {
            createReviewNotice(authorId, article.getId(), article.getTitle(), true, null);
        }
        return article.getId();
    }

    @Transactional
    public void updateArticle(Long userId, ArticleDTO dto) {
        Article article = articleMapper.selectById(dto.getId());
        if (article == null) throw new RuntimeException("文章不存在");
        if (!article.getAuthorId().equals(userId)) throw new RuntimeException("无权修改");
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus());
        article.setIsTop(dto.getIsTop());
        resetReviewFields(article);
        articleMapper.updateById(article);
        saveTags(article.getId(), dto.getTagIds());
        if (Article.STATUS_PUBLISHED == article.getStatus()) {
            createReviewNotice(userId, article.getId(), article.getTitle(), true, null);
        }
    }

    @Transactional
    public void reviewArticle(Long adminId, Long articleId, Integer reviewStatus, String reviewReason) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new RuntimeException("文章不存在");
        if (Article.STATUS_PUBLISHED != article.getStatus()) {
            throw new RuntimeException("仅发布状态文章可审核");
        }
        if (reviewStatus == null || (reviewStatus != Article.REVIEW_APPROVED && reviewStatus != Article.REVIEW_REJECTED)) {
            throw new RuntimeException("审核状态不合法");
        }
        if (reviewStatus == Article.REVIEW_REJECTED && !StringUtils.hasText(reviewReason)) {
            throw new RuntimeException("审核不通过原因不能为空");
        }
        article.setReviewStatus(reviewStatus);
        article.setReviewReason(reviewStatus == Article.REVIEW_REJECTED ? reviewReason.trim() : null);
        article.setReviewedAt(LocalDateTime.now());
        article.setReviewedBy(adminId);
        articleMapper.updateById(article);

        String noticeTitle = reviewStatus == Article.REVIEW_APPROVED ? "文章审核通过" : "文章审核未通过";
        StringBuilder content = new StringBuilder("《" + article.getTitle() + "》审核结果：" + noticeTitle + "。");
        if (reviewStatus == Article.REVIEW_REJECTED) {
            content.append(" 原因：").append(reviewReason.trim());
        }
        createUserMessage(article.getAuthorId(), article.getId(), noticeTitle, content.toString());
    }

    @Transactional
    public void deleteArticle(Long userId, Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new RuntimeException("文章不存在");
        if (!article.getAuthorId().equals(userId)) throw new RuntimeException("无权删除");
        articleTagMapper.deleteByArticleId(articleId);
        articleMapper.deleteById(articleId);
    }

    public void likeArticle(Long id) {
        articleMapper.incrementLikes(id);
    }

    public List<Message> getUserNotifications(Long userId) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getType, Message.TYPE_ARTICLE_REVIEW)
                .orderByDesc(Message::getCreatedAt));
    }

    @Transactional
    public void markNotificationRead(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) throw new RuntimeException("通知不存在");
        if (message.getUserId() == null || !message.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该通知");
        }
        message.setStatus(Message.STATUS_READ);
        messageMapper.updateById(message);
    }

    private IPage<Article> getArticlePage(int page, int size, Long categoryId,
                                          Long tagId, String keyword, Integer status,
                                          Integer reviewStatus, Long authorId) {
        IPage<Article> result = articleMapper.selectArticlePage(
                new Page<>(page, size), categoryId, tagId, keyword, status, reviewStatus, authorId);
        result.getRecords().forEach(a -> {
            a.setTags(tagMapper.selectByArticleId(a.getId()));
            a.setContent(null);
        });
        return result;
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.deleteByArticleId(articleId);
        if (tagIds != null && !tagIds.isEmpty()) {
            tagIds.forEach(tagId -> {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            });
        }
    }

    private void resetReviewFields(Article article) {
        if (Article.STATUS_PUBLISHED == article.getStatus()) {
            article.setReviewStatus(Article.REVIEW_PENDING);
            article.setReviewReason(null);
            article.setReviewedAt(null);
            article.setReviewedBy(null);
        } else {
            article.setReviewStatus(null);
            article.setReviewReason(null);
            article.setReviewedAt(null);
            article.setReviewedBy(null);
        }
    }

    private void createReviewNotice(Long userId, Long articleId, String articleTitle, boolean pending, String reason) {
        String title = pending ? "文章已提交审核" : "文章审核结果通知";
        String content = pending
                ? "《" + articleTitle + "》已提交平台审核，审核通过后才会展示在首页。"
                : reason;
        createUserMessage(userId, articleId, title, content);
    }

    private void createUserMessage(Long userId, Long articleId, String title, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setArticleId(articleId);
        message.setType(Message.TYPE_ARTICLE_REVIEW);
        message.setTitle(title);
        message.setContent(content);
        message.setStatus(Message.STATUS_UNREAD);
        messageMapper.insert(message);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "article_review_notification");
        payload.put("messageId", message.getId());
        payload.put("articleId", articleId);
        payload.put("title", title);
        payload.put("content", content);
        notificationWebSocketHandler.sendToUser(userId, payload);
    }
}
