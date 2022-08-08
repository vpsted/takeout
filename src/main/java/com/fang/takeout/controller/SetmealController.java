package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fang.takeout.common.R;
import com.fang.takeout.dto.SetmealDto;
import com.fang.takeout.entity.Setmeal;
import com.fang.takeout.service.CategoryService;
import com.fang.takeout.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
  @Autowired
  private SetmealService setmealService;
  @Autowired
  private CategoryService categoryService;

  /**
   * 添加套餐以及关联菜品
   *
   * @param setmealDto
   * @return
   */
  @PostMapping
  @CacheEvict(value = "setmealCache",allEntries = true)
  public R<String> save(@RequestBody SetmealDto setmealDto) {
    setmealService.saveWithDish(setmealDto);
    return R.success("添加套餐成功");
  }

  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    Page<Setmeal> setmealPage = new Page<>(page, pageSize);
    LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
    setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name);
    setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
    setmealService.page(setmealPage, setmealLambdaQueryWrapper);
    Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);
    //拷贝除了records以外（类型不匹配，不能直接拷贝）的分页信息
    BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
    //获取含categoryName的SetmealDto集合
    List<SetmealDto> setmealDtoList = setmealPage.getRecords().stream().map((setmeal) -> {
      String categoryName = categoryService.getById(setmeal.getCategoryId()).getName();
      SetmealDto setmealDto = new SetmealDto();
      setmealDto.setCategoryName(categoryName);
      BeanUtils.copyProperties(setmeal, setmealDto);
      return setmealDto;
    }).collect(Collectors.toList());

    setmealDtoPage.setRecords(setmealDtoList);
    return R.success(setmealDtoPage);
  }

  /**
   * 删除套餐，支持批量删除
   *
   * @param ids
   * @return
   */
  @DeleteMapping
  @CacheEvict(value = "setmealCache",allEntries = true)
  public R<String> delete(@RequestParam List<Long> ids) {
    setmealService.deleteWithDish(ids);
    return R.success("删除套餐成功");
  }

  /**
   * 修改套餐售卖状态，支持批量修改
   *
   * @param status
   * @param ids
   * @return
   */
  @PostMapping("/status/{status}")
  @CacheEvict(value = "setmealCache",allEntries = true)
  public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {
    LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.in(ids != null, Setmeal::getId, ids);
    List<Setmeal> setmealList = setmealService.list(lambdaQueryWrapper).stream().map(setmeal -> {
      setmeal.setStatus(status);
      return setmeal;
    }).collect(Collectors.toList());
    setmealService.updateBatchById(setmealList);
    return R.success("修改状态成功");
  }

  @GetMapping("/list")
  @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
  public R<List<Setmeal>> list(Setmeal setmeal) {
    LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
    queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
    queryWrapper.orderByDesc(Setmeal::getUpdateTime);
    List<Setmeal> setmealList = setmealService.list(queryWrapper);
    return R.success(setmealList);
  }
}
