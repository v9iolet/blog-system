package com.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String from;

    private static final String CODE_PREFIX = "email:code:";
    private static final String LIMIT_PREFIX = "email:limit:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_LIMIT_SECONDS = 60;

    /**
     * 发送验证码
     */
    public void sendVerificationCode(String toEmail, String purpose) {
        // 检查发送频率限制
        String limitKey = LIMIT_PREFIX + toEmail;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }

        // 生成6位数字验证码
        String code = generateCode();

        // 存入Redis，5分钟过期
        String codeKey = CODE_PREFIX + purpose + ":" + toEmail;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送频率限制（60秒内不能重复发送）
        redisTemplate.opsForValue().set(limitKey, "1", SEND_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            
            if ("register".equals(purpose)) {
                message.setSubject("博客系统 - 注册验证码");
                message.setText("您正在注册博客系统账号，验证码是：" + code + "\n\n验证码" + CODE_EXPIRE_MINUTES + "分钟内有效，请勿泄露给他人。\n\n如非本人操作，请忽略此邮件。");
            } else if ("reset".equals(purpose)) {
                message.setSubject("博客系统 - 找回密码验证码");
                message.setText("您正在找回博客系统账号密码，验证码是：" + code + "\n\n验证码" + CODE_EXPIRE_MINUTES + "分钟内有效，请勿泄露给他人。\n\n如非本人操作，请立即修改密码。");
            }

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}, 用途: {}", toEmail, purpose);
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            throw new RuntimeException("发送邮件失败，请检查邮箱地址是否正确");
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code, String purpose) {
        String codeKey = CODE_PREFIX + purpose + ":" + email;
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (savedCode == null) {
            return false;
        }
        
        // 验证成功后删除验证码
        if (savedCode.equals(code)) {
            redisTemplate.delete(codeKey);
            return true;
        }
        
        return false;
    }

    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        return String.format("%0" + CODE_LENGTH + "d", random.nextInt((int) Math.pow(10, CODE_LENGTH)));
    }
}
