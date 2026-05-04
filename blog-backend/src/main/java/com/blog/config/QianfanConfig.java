package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度千帆配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "blog.ai.qianfan")
public class QianfanConfig {
    
    /**
     * API Key (v3 版本)
     */
    private String apiKey;
    
    /**
     * ERNIE 模型 API 地址
     */
    private String modelUrl;
    
    /**
     * 是否启用 AI 功能
     */
    private Boolean enabled = true;
}
