package com.blog.controller;

import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.service.CategoryTagService;
import com.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryTagController {

    private final CategoryTagService categoryTagService;

    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        return Result.success(categoryTagService.getAllCategories());
    }

    @GetMapping("/tags")
    public Result<List<Tag>> getTags() {
        return Result.success(categoryTagService.getAllTags());
    }
}
