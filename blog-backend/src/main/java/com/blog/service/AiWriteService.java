package com.blog.service;

import com.blog.dto.ArticleAiRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 写作辅助服务
 * 使用 Spring AI 框架调用大语言模型（百度千帆 deepseek-v4-flash），
 * 根据用户提供的标题、分类和标签，流式生成 Markdown 格式的文章草稿。
 */
@Slf4j
@Service
public class AiWriteService {

    /** System Prompt：定义模型的角色和输出规范 */
    private static final String SYSTEM_PROMPT = """
            你是一位专业的技术博主，擅长撰写结构清晰、内容深入的博客文章。
            根据用户提供的文章标题、分类和标签，生成一篇完整的 Markdown 格式文章草稿。

            要求：
            1. 文章结构包含引言、正文（多个小节）、总结
            2. 使用 Markdown 标题（##、###）组织内容层次
            3. 适当使用列表、代码块、引用等 Markdown 元素增强可读性
            4. 语言风格专业但不晦涩，适合技术博客读者
            5. 直接输出 Markdown 内容，不要添加额外的说明或元信息
            6. 文章长度适中（800-1500字左右）
            """;

    private final ChatClient chatClient;

    /**
     * 通过构造函数注入 Spring AI 的 ChatModel，并构建 ChatClient。
     * ChatModel 由 spring-ai-openai-spring-boot-starter 自动配置，
     * 读取 application.yml 中 spring.ai.openai 的连接信息。
     */
    public AiWriteService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 流式生成文章草稿
     *
     * @param request 包含标题、分类、标签的请求参数
     * @return Flux<String> 每个元素为一小段文本，前端可逐段拼接实现实时展示
     */
    public Flux<String> generateDraft(ArticleAiRequestDTO request) {
        // 构建用户消息：将标题、分类、标签格式化为清晰的指令
        String userMessage = buildUserMessage(request);

        log.info("开始 AI 流式生成草稿，标题: {}, 分类: {}, 标签: {}",
                request.getTitle(), request.getCategoryName(), request.getTagNames());

        // 调用 Spring AI ChatClient 的流式接口
        // .stream().content() 返回 Flux<String>，每个元素是一段文本片段
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * 将请求参数组装为用户消息文本
     */
    private String buildUserMessage(ArticleAiRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为以下主题撰写一篇博客文章草稿：\n\n");
        sb.append("【标题】").append(request.getTitle()).append("\n");

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            sb.append("【分类】").append(request.getCategoryName()).append("\n");
        }

        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            sb.append("【标签】").append(String.join("、", request.getTagNames())).append("\n");
        }

        return sb.toString();
    }
}
