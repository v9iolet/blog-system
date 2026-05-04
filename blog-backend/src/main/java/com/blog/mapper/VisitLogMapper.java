package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.VisitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {
    @Select("SELECT COUNT(DISTINCT ip) FROM visit_log")
    Long countUniqueVisitors();

    @Select("SELECT COUNT(*) FROM visit_log")
    Long countTotalVisits();
}
