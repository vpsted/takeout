package com.fang.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fang.takeout.dto.SetmealDto;
import com.fang.takeout.entity.Setmeal;
import com.fang.takeout.mapper.SetmealMapper;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
  public void saveWithDish(SetmealDto setmealDto);
  public void deleteWithDish(List<Long> ids);
}
