package com.blog.service;

import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryTagService {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public List<Category> getAllCategories() {
        return categoryMapper.selectAllWithCount();
    }

    public Category createCategory(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        categoryMapper.insert(c);
        return c;
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    public List<Tag> getAllTags() {
        return tagMapper.selectAllWithCount();
    }

    public Tag createTag(String name) {
        Tag t = new Tag();
        t.setName(name);
        tagMapper.insert(t);
        return t;
    }

    public void deleteTag(Long id) {
        tagMapper.deleteById(id);
    }
}
