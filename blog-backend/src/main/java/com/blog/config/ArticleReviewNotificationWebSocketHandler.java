package com.blog.config;

import com.blog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ArticleReviewNotificationWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ArticleReviewNotificationWebSocketHandler(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = resolveUserId(session.getUri());
        if (userId != null) {
            sessions.put(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    public void sendToUser(Long userId, Map<String, Object> payload) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (IOException e) {
            sessions.remove(userId);
        }
    }

    private Long resolveUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && "token".equals(pair[0])) {
                try {
                    String token = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    return jwtUtil.getUserId(token);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
