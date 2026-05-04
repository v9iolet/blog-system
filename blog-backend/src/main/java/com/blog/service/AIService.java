package com.blog.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.blog.config.QianfanConfig;
import com.blog.dto.AISummaryRequest;
import com.blog.dto.AISummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务 - 基于百度千帆 ERNIE 4.5 Turbo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final QianfanConfig qianfanConfig;
    private final StringRedisTemplate redisTemplate;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final String CACHE_PREFIX = "ai:summary:";
    private static final long CACHE_EXPIRE_DAYS = 30;

    /**
     * 生成文章摘要
     */
    public AISummaryResponse generateSummary(AISummaryRequest request) {
        if (!qianfanConfig.getEnabled()) {
            throw new RuntimeException("AI 功能未启用");
        }

        // 生成缓存 key
        String cacheKey = generateCacheKey(request.getTitle(), request.getContent());
        
        // 尝试从缓存获取
        String cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cachedResult)) {
            log.info("从缓存获取 AI 摘要");
            AISummaryResponse response = JSONUtil.toBean(cachedResult, AISummaryResponse.class);
            response.setFromCache(true);
            return response;
        }

        // 调用百度千帆 ERNIE API
        try {
            String summary = callErnieModel(request);
            List<String> keywords = extractKeywords(summary, request.getContent());
            
            AISummaryResponse response = new AISummaryResponse(summary, keywords, false);
            
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(response), 
                    CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            
            return response;
        } catch (Exception e) {
            log.error("调用百度千帆 ERNIE API 失败", e);
            // 降级策略：使用简单的文本摘要
            return generateFallbackSummary(request);
        }
    }

    /**
     * 调用百度千帆 ERNIE 4.5 Turbo 模型
     */
    private String callErnieModel(AISummaryRequest request) throws IOException {
        // 构建提示词
        String userPrompt = buildPrompt(request);
        
        // 构建请求体 - 符合百度千帆 v2 API 格式
        JSONObject requestBody = new JSONObject();
        
        // 构建 messages 数组
        JSONArray messages = new JSONArray();
        
        // 用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        requestBody.set("model", "ernie-4.5-turbo");  // 指定模型
        requestBody.set("temperature", 0.3);  // 较低的温度，使输出更确定
        requestBody.set("top_p", 0.8);
        requestBody.set("max_tokens", 500);  // 限制输出长度
        
        log.info("调用 ERNIE API，URL: {}", qianfanConfig.getModelUrl());
        log.info("请求体: {}", requestBody.toString());
        
        // 构建请求
        Request httpRequest = new Request.Builder()
                .url(qianfanConfig.getModelUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", qianfanConfig.getApiKey())
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        // 发送请求
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                log.error("API 请求失败: {} - {}, 响应: {}", response.code(), response.message(), errorBody);
                throw new IOException("请求失败: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            log.info("百度千帆 ERNIE API 响应: {}", responseBody);
            
            // 解析响应
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
            
            // 百度千帆 v2 API 响应格式: {"choices": [{"message": {"content": "..."}}]}
            if (jsonResponse.containsKey("choices")) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && choices.size() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    if (message != null && message.containsKey("content")) {
                        return message.getStr("content").trim();
                    }
                }
            }
            // 兼容旧格式
            else if (jsonResponse.containsKey("result")) {
                return jsonResponse.getStr("result").trim();
            } 
            else if (jsonResponse.containsKey("error_code")) {
                String errorMsg = jsonResponse.getStr("error_msg", "未知错误");
                throw new IOException("API 返回错误: " + errorMsg);
            } 
            
            throw new IOException("无法解析 API 响应: " + responseBody);
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(AISummaryRequest request) {
        int summaryLength = request.getSummaryLength() != null ? request.getSummaryLength() : 150;
        
        // 清理 Markdown 格式，只保留纯文本
        String cleanContent = cleanMarkdown(request.getContent());
        
        // 如果内容太长，截取前 2000 字
        if (cleanContent.length() > 2000) {
            cleanContent = cleanContent.substring(0, 2000) + "...";
        }
        
        return String.format(
            "请为以下文章生成一个约%d字的摘要，要求:\n" +
            "1. 简洁准确地概括文章核心内容\n" +
            "2. 语言流畅，逻辑清晰\n" +
            "3. 直接输出摘要内容，不要包含摘要等前缀\n\n" +
            "文章标题: %s\n\n" +
            "文章内容:\n%s",
            summaryLength,
            request.getTitle(),
            cleanContent
        );
    }

    /**
     * 清理 Markdown 格式
     */
    private String cleanMarkdown(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        
        String cleaned = content;
        
        // 移除代码块
        cleaned = cleaned.replaceAll("```[\\s\\S]*?```", "");
        
        // 移除行内代码
        cleaned = cleaned.replaceAll("`[^`]+`", "");
        
        // 移除图片
        cleaned = cleaned.replaceAll("!\\[.*?\\]\\(.*?\\)", "");
        
        // 移除链接，保留文本
        cleaned = cleaned.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        
        // 移除标题标记
        cleaned = cleaned.replaceAll("#{1,6}\\s+", "");
        
        // 移除加粗和斜体
        cleaned = cleaned.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("\\*([^*]+)\\*", "$1");
        cleaned = cleaned.replaceAll("__([^_]+)__", "$1");
        cleaned = cleaned.replaceAll("_([^_]+)_", "$1");
        
        // 移除列表标记
        cleaned = cleaned.replaceAll("(?m)^[\\s]*[-*+]\\s+", "");
        cleaned = cleaned.replaceAll("(?m)^[\\s]*\\d+\\.\\s+", "");
        
        // 移除多余空白
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        cleaned = cleaned.trim();
        
        return cleaned;
    }

    /**
     * 提取关键词
     */
    private List<String> extractKeywords(String summary, String content) {
        List<String> keywords = new ArrayList<>();
        
        // 简单的关键词提取：查找常见技术词汇
        String[] techKeywords = {
            "Java", "Python", "JavaScript", "TypeScript", "Vue", "React", "Spring", "SpringBoot",
            "MySQL", "Redis", "Docker", "Kubernetes", "微服务", "分布式", "高并发", "性能优化",
            "算法", "数据结构", "设计模式", "架构", "前端", "后端", "全栈", "AI", "机器学习",
            "深度学习", "大数据", "云计算", "DevOps", "敏捷开发", "测试", "安全"
        };
        
        String combinedText = summary + " " + content;
        for (String keyword : techKeywords) {
            if (combinedText.contains(keyword) && !keywords.contains(keyword)) {
                keywords.add(keyword);
                if (keywords.size() >= 5) {
                    break;
                }
            }
        }
        
        return keywords;
    }

    /**
     * 生成缓存 key
     */
    private String generateCacheKey(String title, String content) {
        String combined = title + content;
        String hash = DigestUtil.md5Hex(combined);
        return CACHE_PREFIX + hash;
    }

    /**
     * 降级策略：生成简单摘要
     */
    private AISummaryResponse generateFallbackSummary(AISummaryRequest request) {
        log.warn("使用降级策略生成摘要");
        
        String content = cleanMarkdown(request.getContent());
        
        // 简单截取前 150 字作为摘要
        int length = Math.min(content.length(), request.getSummaryLength() != null ? 
                request.getSummaryLength() : 150);
        String summary = content.substring(0, length);
        
        if (content.length() > length) {
            summary += "...";
        }
        
        List<String> keywords = extractKeywords(summary, content);
        
        return new AISummaryResponse(summary, keywords, false);
    }
}
