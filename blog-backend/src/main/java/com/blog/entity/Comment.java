package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private Long parentId;
    private Integer likes;
    private Integer status;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private java.util.List<Comment> children;
}
