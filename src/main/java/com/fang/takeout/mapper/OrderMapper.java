package com.fang.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fang.takeout.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

}