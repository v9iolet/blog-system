package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    public static final int TYPE_GUESTBOOK = 0;
    public static final int TYPE_ARTICLE_REVIEW = 1;
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long articleId;
    private Integer type;
    private String title;
    private String nickname;
    private String email;
    private String content;
    private String reply;
    private Integer status; // 0-未读/未回复 1-已读/已回复
    private LocalDateTime createdAt;
}
