package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer role;  // 0-普通用户 1-管理员
    private Integer status; // 0-禁用 1-正常
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
