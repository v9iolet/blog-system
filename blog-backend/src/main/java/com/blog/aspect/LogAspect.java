package com.blog.aspect;

import com.blog.entity.SysLog;
import com.blog.mapper.SysLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper sysLogMapper;

    @Around("execution(* com.blog.controller.AdminController.*(..))")
    public Object logAdminAction(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        long cost = System.currentTimeMillis() - start;

        try {
            SysLog sysLog = new SysLog();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long) {
                sysLog.setUserId((Long) auth.getPrincipal());
            }
            sysLog.setAction(point.getSignature().getName());
            sysLog.setDetail("耗时: " + cost + "ms");
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                sysLog.setIp(req.getRemoteAddr());
            }
            sysLogMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("记录日志失败", e);
        }
        return result;
    }
}
