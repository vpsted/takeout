package com.fang.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fang.takeout.dto.DishDto;
import com.fang.takeout.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
  public void saveWithFlavor(DishDto dishDto);
  public DishDto getByIdWithFlavor(Long id);
  public void updateWithFlavor(DishDto dishDto);
  public void deleteWithFlavor(List<Long> ids);
}
