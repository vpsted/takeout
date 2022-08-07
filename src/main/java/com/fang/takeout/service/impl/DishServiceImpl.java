package com.fang.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fang.takeout.common.CustomException;
import com.fang.takeout.common.R;
import com.fang.takeout.dto.DishDto;
import com.fang.takeout.entity.Dish;
import com.fang.takeout.entity.DishFlavor;
import com.fang.takeout.mapper.DishMapper;
import com.fang.takeout.service.DishFlavorService;
import com.fang.takeout.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service//由spring管理
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
  @Autowired
  private DishFlavorService dishFlavorService;

  /**
   * 添加菜品以及关联的口味
   *
   * @param dishDto
   */
  @Override
  @Transactional//因为同一个方法中多次进行数据操作，通过将多个事务合并成一个事务，当任意一个事务异常时，整个大的事务都回滚
  public void saveWithFlavor(DishDto dishDto) {
    this.save(dishDto);
    Long dishId = dishDto.getId();
    List<DishFlavor> flavors = dishDto.getFlavors();
    flavors = flavors.stream().map((item) -> {
      item.setDishId(dishId);
      return item;
    }).collect(Collectors.toList());
    dishFlavorService.saveBatch(flavors);
  }

  @Override
  public DishDto getByIdWithFlavor(Long id) {
    LambdaQueryWrapper<DishFlavor> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
    dishLambdaQueryWrapper.eq(DishFlavor::getDishId, id);
    List<DishFlavor> dishFlavorList = dishFlavorService.list(dishLambdaQueryWrapper);
    Dish dish = this.getById(id);
    DishDto dishDto = new DishDto();
    BeanUtils.copyProperties(dish, dishDto);
    dishDto.setFlavors(dishFlavorList);
    return dishDto;
  }

  @Override
  @Transactional
  public void updateWithFlavor(DishDto dishDto) {
    this.updateById(dishDto);
    LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
    //修改口味，可能会删除原来的某些口味，这里一次性全部删除先
    queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
    dishFlavorService.remove(queryWrapper);
    List<DishFlavor> list = dishDto.getFlavors().stream().map((dishFlavor) -> {
      dishFlavor.setDishId(dishDto.getId());
      return dishFlavor;
    }).collect(Collectors.toList());
    dishFlavorService.saveBatch(list);
  }

  @Override
  @Transactional
  public void deleteWithFlavor(List<Long> ids) {
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.in(ids!=null,Dish::getId, ids);
    List<Dish> dishList = this.list(queryWrapper);

    for (Dish dish : dishList) {
      if (dish.getStatus() == 1) {
        throw new CustomException("待删除菜品中有正在售卖的菜品，删除失败");
      }
      LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
      //修改口味，可能会删除原来的某些口味，这里一次性全部删除先
      dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
      dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
      this.removeById(dish.getId());
    }
  }

}
