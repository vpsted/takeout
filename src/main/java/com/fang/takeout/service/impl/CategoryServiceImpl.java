package com.fang.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fang.takeout.common.CustomException;
import com.fang.takeout.entity.Category;
import com.fang.takeout.entity.Dish;
import com.fang.takeout.entity.Setmeal;
import com.fang.takeout.mapper.CategoryMapper;
import com.fang.takeout.service.CategoryService;
import com.fang.takeout.service.DishService;
import com.fang.takeout.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service//由spring管理
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
  @Autowired
  private DishService dishService;
  @Autowired
  private SetmealService setmealService;

  /**
   * 根据id删除分类，删除前要判断该分类是否关联了其它菜品或套餐
   * @param id
   */
  @Override
  public void remove(Long id) {
    //查询是否关联dish
    LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
    dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
    int dishCount=dishService.count(dishLambdaQueryWrapper);
if(dishCount>0){
  throw new CustomException("当前分类已关联菜品，不能删除，请先解除关联");
}
    //查询是否关联setmeal
    LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
    setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
    int setmealCount=setmealService.count(setmealLambdaQueryWrapper);
    if(setmealCount>0){
      throw new CustomException("当前分类已关联套餐，不能删除，请先解除关联");
    }
    super.removeById(id);
  }
}
