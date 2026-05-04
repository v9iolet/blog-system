package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 摘要生成请求
 */
@Data
public class AISummaryRequest {
    
    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    /**
     * 摘要长度（字数）
     */
    private Integer summaryLength = 150;
}
