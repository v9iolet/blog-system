package com.blog.controller;

import com.blog.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new RuntimeException("文件不能为空");
        
        String originalName = file.getOriginalFilename();
        String ext = originalName != null ? originalName.substring(originalName.lastIndexOf(".")) : ".png";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        
        // 获取 src/main/resources/uploads 目录
        String projectPath = System.getProperty("user.dir");
        Path uploadDir = Paths.get(projectPath, "src", "main", "resources", "uploads");
        
        // 如果目录不存在则创建
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath.toFile());
        
        return Result.success("/uploads/" + fileName);
    }
}
