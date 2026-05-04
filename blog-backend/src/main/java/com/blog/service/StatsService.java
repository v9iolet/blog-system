package com.blog.service;

import com.blog.entity.VisitLog;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.mapper.VisitLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final VisitLogMapper visitLogMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    public void recordVisit(Long articleId, HttpServletRequest request) {
        VisitLog log = new VisitLog();
        log.setArticleId(articleId);
        log.setIp(getClientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setReferer(request.getHeader("Referer"));
        visitLogMapper.insert(log);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", articleMapper.selectCount(null));
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalComments", commentMapper.selectCount(null));
        stats.put("totalVisits", visitLogMapper.countTotalVisits());
        stats.put("uniqueVisitors", visitLogMapper.countUniqueVisitors());
        return stats;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
