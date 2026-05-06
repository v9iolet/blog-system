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
     * API Key (旧格式，兼容性保留)
     * 格式: bce-v3/ACCESS_KEY_ID/SECRET_ACCESS_KEY
     */
    private String apiKey;

    /**
     * Access Key ID (百度千帆)
     */
    private String accessKey;

    /**
     * Secret Access Key (百度千帆)
     */
    private String secretKey;

    /**
     * ERNIE 模型 API 地址
     */
    private String modelUrl;

    /**
     * 是否启用 AI 功能
     */
    private Boolean enabled = true;
}
