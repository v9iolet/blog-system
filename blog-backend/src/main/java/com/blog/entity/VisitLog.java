package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visit_log")
public class VisitLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String ip;
    private String userAgent;
    private String referer;
    private LocalDateTime createdAt;
}
