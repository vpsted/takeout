package com.fang.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fang.takeout.entity.SetmealDish;
import com.fang.takeout.mapper.SetmealDishMapper;
import com.fang.takeout.mapper.SetmealMapper;
import com.fang.takeout.service.SetmealDishService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
