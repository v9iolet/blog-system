package com.blog.controller;

import com.blog.dto.ArticleAiRequestDTO;
import com.blog.service.AiWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 写作辅助控制器
 * 提供流式（SSE）接口，前端可实时接收生成的文章草稿内容。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiWriteController {

    private final AiWriteService aiWriteService;

    /**
     * 流式生成文章草稿
     *
     * 使用 ServerSentEvent 包装每个文本片段，确保 SSE 格式标准化：
     * - 每条消息格式为: data:{文本}\n\n
     * - 结束时发送: data:[DONE]\n\n
     */
    @PostMapping(value = "/generate-draft", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateDraft(@Validated @RequestBody ArticleAiRequestDTO request) {
        log.info("收到 AI 草稿生成请求: title={}, category={}, tags={}",
                request.getTitle(), request.getCategoryName(), request.getTagNames());

        return aiWriteService.generateDraft(request)
                .map(chunk -> ServerSentEvent.builder(chunk).build())
                // 追加一个 [DONE] 事件，告知前端流式传输结束
                .concatWithValues(ServerSentEvent.builder("[DONE]").build());
    }
}
