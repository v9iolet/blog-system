package com.blog.controller;

import com.blog.dto.AISummaryRequest;
import com.blog.dto.AISummaryResponse;
import com.blog.util.Result;
import com.blog.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI 功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * 生成文章摘要
     */
    @PostMapping("/summary")
    public Result<AISummaryResponse> generateSummary(@Validated @RequestBody AISummaryRequest request) {
        log.info("收到 AI 摘要生成请求，标题: {}", request.getTitle());
        
        try {
            AISummaryResponse response = aiService.generateSummary(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return Result.error("生成摘要失败: " + e.getMessage());
        }
    }

    /**
     * 检查 AI 功能是否可用
     */
    @GetMapping("/status")
    public Result<Boolean> checkStatus() {
        return Result.success(true);
    }
}
