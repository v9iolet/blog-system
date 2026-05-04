package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    IPage<Article> selectArticlePage(Page<Article> page,
                                     @Param("categoryId") Long categoryId,
                                     @Param("tagId") Long tagId,
                                     @Param("keyword") String keyword,
                                     @Param("status") Integer status,
                                     @Param("reviewStatus") Integer reviewStatus,
                                     @Param("authorId") Long authorId);

    Article selectArticleDetail(@Param("id") Long id);

    @Update("UPDATE article SET views = views + 1 WHERE id = #{id}")
    void incrementViews(@Param("id") Long id);

    @Update("UPDATE article SET likes = likes + 1 WHERE id = #{id}")
    void incrementLikes(@Param("id") Long id);
}
