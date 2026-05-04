package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private List<Long> tagIds;
    private Integer status; // 0-草稿 1-发布
    private Integer isTop;
}
