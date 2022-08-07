package com.fang.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fang.takeout.common.CustomException;
import com.fang.takeout.dto.SetmealDto;
import com.fang.takeout.entity.Dish;
import com.fang.takeout.entity.DishFlavor;
import com.fang.takeout.entity.Setmeal;
import com.fang.takeout.entity.SetmealDish;
import com.fang.takeout.mapper.SetmealMapper;
import com.fang.takeout.service.SetmealDishService;
import com.fang.takeout.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
  @Autowired
  private SetmealDishService setmealDishService;
  @Value("${takeout.path}")
  private String PATH;

  @Override
  @Transactional//修改了两张表，要保证事务的一致性
  public void saveWithDish(SetmealDto setmealDto) {
    //
    this.save(setmealDto);
    List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
    setmealDishes.stream().map((setmealDish) -> {
      setmealDish.setSetmealId(setmealDto.getId());
      return setmealDish;
    }).collect(Collectors.toList());
    setmealDishService.saveBatch(setmealDishes);
  }

  @Override
  @Transactional
  public void deleteWithDish(List<Long> ids) {
    LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.in(ids != null, Setmeal::getId, ids);
    queryWrapper.eq(Setmeal::getStatus, 1);

    int count = this.count(queryWrapper);
    if (count > 0) {
      throw new CustomException("待删除套餐中有正在售卖的套餐，删除失败");
    }

    this.removeByIds(ids);
    //删除套餐关联的菜品
    LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
    setmealDishLambdaQueryWrapper.in(ids != null, SetmealDish::getSetmealId, ids);
    setmealDishService.remove(setmealDishLambdaQueryWrapper);
  }
}
