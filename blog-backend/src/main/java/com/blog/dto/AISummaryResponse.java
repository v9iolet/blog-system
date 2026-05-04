package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 摘要生成响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AISummaryResponse {
    
    /**
     * 生成的摘要
     */
    private String summary;
    
    /**
     * 推荐的关键词
     */
    private List<String> keywords;
    
    /**
     * 是否来自缓存
     */
    private Boolean fromCache = false;
}
