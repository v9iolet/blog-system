package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article_tag")
public class ArticleTag {
    private Long articleId;
    private Long tagId;
}
