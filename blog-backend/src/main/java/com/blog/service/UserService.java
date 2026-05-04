package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername())
                        .last("LIMIT 1"));
        if (user == null || !matchesPassword(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getRole() == null || !user.getRole().equals(dto.getRole())) {
            throw new RuntimeException("登录角色与账号类型不匹配");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", sanitizeUser(user));
        return result;
    }

    public void register(RegisterDTO dto) {
        // 验证邮箱验证码
        if (!emailService.verifyCode(dto.getEmail(), dto.getCode(), "register")) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail())) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    public void resetPassword(String email, String code, String newPassword) {
        // 验证邮箱验证码
        if (!emailService.verifyCode(email, code, "reset")) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, email)
                        .last("LIMIT 1"));
        
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public User getById(Long id) {
        return sanitizeUser(userMapper.selectById(id));
    }

    public void updateProfile(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        userMapper.updateById(user);
    }

    public void changePassword(Long userId, String oldPwd, String newPwd) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(user);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    private User sanitizeUser(User user) {
        if (user != null) user.setPassword(null);
        return user;
    }
}
