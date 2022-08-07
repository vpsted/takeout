package com.fang.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fang.takeout.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//由mybatis-plus实现
public interface CategoryMapper extends BaseMapper<Category> {
}
