package com.blog.controller;

import com.blog.dto.UserUpdateDTO;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<User> getProfile(Authentication auth) {
        Long userId = requireUserId(auth);
        return Result.success(userService.getById(userId));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody UserUpdateDTO dto) {
        Long userId = requireUserId(auth);
        userService.updateProfile(userId, dto);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = requireUserId(auth);
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new AuthenticationCredentialsNotFoundException("请先登录");
        }
        return userId;
    }
}
