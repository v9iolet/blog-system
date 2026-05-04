package com.blog.controller;

import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.ResetPasswordDTO;
import com.blog.service.EmailService;
import com.blog.service.UserService;
import com.blog.util.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/send-code")
    public Result<Void> sendVerificationCode(
            @RequestParam @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
            @RequestParam @NotBlank(message = "用途不能为空") String purpose) {
        
        if (!"register".equals(purpose) && !"reset".equals(purpose)) {
            return Result.error("无效的用途参数");
        }
        
        emailService.sendVerificationCode(email, purpose);
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return Result.success();
    }
}
