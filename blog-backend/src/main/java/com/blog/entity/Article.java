package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("article")
public class Article {
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int REVIEW_PENDING = 0;
    public static final int REVIEW_APPROVED = 1;
    public static final int REVIEW_REJECTED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Long authorId;
    private Long categoryId;
    private Integer views;
    private Integer likes;
    private Integer status; // 0-草稿 1-已发布
    private Integer reviewStatus; // 0-待审核 1-审核通过 2-审核不通过
    private String reviewReason;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private Integer isTop;  // 0-否 1-置顶
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private List<Tag> tags;
}
