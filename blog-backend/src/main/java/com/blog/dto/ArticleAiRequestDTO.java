package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * AI 辅助生成文章草稿的请求 DTO
 * 前端传入文章标题、分类名称和标签列表，后端据此生成 Markdown 草稿
 */
@Data
public class ArticleAiRequestDTO {

    /** 文章标题（必填） */
    @NotBlank(message = "文章标题不能为空")
    private String title;

    /** 文章分类名称（可选） */
    private String categoryName;

    /** 文章标签列表（可选） */
    private List<String> tagNames;
}
