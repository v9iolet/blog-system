package com.blog.controller;

import com.blog.dto.UserUpdateDTO;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.util.Result;
import lombok.RequiredArgsConstructor;
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
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getById(userId));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody UserUpdateDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateProfile(userId, dto);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }
}
