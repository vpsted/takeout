package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fang.takeout.common.R;
import com.fang.takeout.dto.DishDto;
import com.fang.takeout.entity.Dish;
import com.fang.takeout.entity.DishFlavor;
import com.fang.takeout.service.CategoryService;
import com.fang.takeout.service.DishFlavorService;
import com.fang.takeout.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
  @Autowired
  private DishService dishService;
  @Autowired
  private DishFlavorService dishFlavorService;
  @Autowired
  private CategoryService categoryService;

  /**
   * 新增菜品
   *
   * @param dishDto
   * @return
   */
  @PostMapping
  @CacheEvict(value = "dishCache",allEntries = true)
  public R<String> save(@RequestBody DishDto dishDto) {
    dishService.saveWithFlavor(dishDto);
    return R.success("新增菜品成功");
  }

  /**
   * 菜品分页查询
   *
   * @param page
   * @param pageSize
   * @param name
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    Page<Dish> dishPage = new Page<>(page, pageSize);
    Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.like(name != null, Dish::getName, name);
    queryWrapper.orderByDesc(Dish::getUpdateTime);
    dishService.page(dishPage, queryWrapper);
    //拷贝分页查询结果
    BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
    List<Dish> dishList = dishPage.getRecords();
    List<DishDto> dishDtoList = dishList.stream().map(dish -> {
      //获取dish对应的categoryId
      Long categoryId = dish.getCategoryId();
      //获取categoryId对应的categoryName
      String categoryName = categoryService.getById(categoryId).getName();
      //创建dishDto并拷贝数据
      DishDto dishDto = new DishDto();
      BeanUtils.copyProperties(dish, dishDto);
      dishDto.setCategoryName(categoryName);
      return dishDto;
    }).collect(Collectors.toList());
    dishDtoPage.setRecords(dishDtoList);
    return R.success(dishDtoPage);
  }

  /**
   * 通过id查询dish及其关联口味
   *
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  @Cacheable(value = "dishCache",key = "'oneDish'+#id")
  public R<DishDto> getById(@PathVariable Long id) {
    DishDto dishDto = dishService.getByIdWithFlavor(id);
    return R.success(dishDto);
  }

  /**
   * 修改菜品
   *
   * @param dishDto
   * @return
   */
  @PutMapping
  @CacheEvict(value = "dishCache",allEntries = true)
  public R<String> update(@RequestBody DishDto dishDto) {
    dishService.updateWithFlavor(dishDto);
    return R.success("修改菜品成功");
  }

  @DeleteMapping
  @CacheEvict(value = "dishCache",allEntries = true)
  public R<String> delete(@RequestParam("ids") List<Long> ids) {
    dishService.deleteWithFlavor(ids);
    return R.success("删除菜品成功");
  }

  /**
   * 更新菜品的状态
   *
   * @param status
   * @param ids
   * @return
   */
  @PostMapping("/status/{status}")
  @CacheEvict(value = "dishCache",allEntries = true)
  public R<String> updateStatus(@PathVariable int status, @RequestParam("ids") List<Long> ids) {
    LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.in(ids != null, Dish::getId, ids);
    List<Dish> dishList = dishService.list(lambdaQueryWrapper);
    dishList = dishList.stream().map((dish) -> {
      dish.setStatus(status);
      return dish;
    }).collect(Collectors.toList());
    if (dishService.updateBatchById(dishList)) {
      return R.success("修改菜品售卖状态成功");
    }
    return R.error("修改菜品售卖状态失败");
  }

  /**
   * 根据条件查询对应的菜品
   *
   * @param dish
   * @return
   */
  @GetMapping("/list")
  @Cacheable(value = "dishCache",key = "#dish.categoryId")
  public R<List<DishDto>> list(Dish dish) {
    LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
    //停售的菜品不显示,只查询起售(status=1)的菜品
    lambdaQueryWrapper.eq(Dish::getStatus, 1);
    List<Dish> dishList = dishService.list(lambdaQueryWrapper);

    List<DishDto> dishDtoList = dishList.stream().map((item) -> {
      //当前菜品id
      Long id = item.getId();
      LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
      flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, id);
      List<DishFlavor> dishFlavorList = dishFlavorService.list(flavorLambdaQueryWrapper);
      DishDto dishDto = new DishDto();
      dishDto.setFlavors(dishFlavorList);
      BeanUtils.copyProperties(item, dishDto);
      return dishDto;
    }).collect(Collectors.toList());
    return R.success(dishDtoList);
  }
}
