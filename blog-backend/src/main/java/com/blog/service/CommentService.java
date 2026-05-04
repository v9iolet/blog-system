package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    public List<Comment> getCommentTree(Long articleId) {
        List<Comment> all = commentMapper.selectCommentTree(articleId);
        Map<Long, Comment> map = all.stream().collect(Collectors.toMap(Comment::getId, c -> c));
        List<Comment> roots = new ArrayList<>();
        all.forEach(c -> {
            if (c.getParentId() == null) {
                c.setChildren(new ArrayList<>());
                roots.add(c);
            } else {
                Comment parent = map.get(c.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(c);
                }
            }
        });
        return roots;
    }

    public void addComment(Long userId, Long articleId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setStatus(1);
        comment.setLikes(0);
        commentMapper.insert(comment);
    }

    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new RuntimeException("评论不存在");
        if (!comment.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        commentMapper.deleteById(commentId);
    }

    public long countByArticle(Long articleId) {
        return commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, articleId));
    }
}
