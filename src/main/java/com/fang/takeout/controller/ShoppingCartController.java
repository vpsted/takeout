package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fang.takeout.common.BaseContext;
import com.fang.takeout.common.R;
import com.fang.takeout.entity.ShoppingCart;
import com.fang.takeout.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

  @Autowired
  private ShoppingCartService shoppingCartService;

  /**
   * 向购物车添加菜品或套餐
   *
   * @param shoppingCart
   * @return
   */
  @PostMapping("/add")
  public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
    Long userId = BaseContext.getCurrentId();
    shoppingCart.setUserId(userId);
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, userId);
    Long dishId = shoppingCart.getDishId();
    if (dishId != null) {
      // 添加到购物车的是菜品
      queryWrapper.eq(ShoppingCart::getDishId, dishId);
    } else {
      queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
    }
    ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
    if (shoppingCart1 != null) {
      //菜品或套餐已存在，数量加一
      shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
      shoppingCartService.updateById(shoppingCart1);
      return R.success(shoppingCart1);
    } else {
      //菜品或套餐不存在
      shoppingCart.setNumber(1);
      shoppingCart.setCreateTime(LocalDateTime.now());
      shoppingCartService.save(shoppingCart);
      return R.success(shoppingCart);
    }
  }

  @GetMapping("/list")
  public R<List<ShoppingCart>> list() {
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
    queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
    List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
    return R.success(shoppingCartList);
  }

  /**
   * 减少或删除购物车中的菜品或套餐
   *
   * @param shoppingCart
   * @return
   */
  @PostMapping("/sub")
  public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
    Long userId = BaseContext.getCurrentId();
    shoppingCart.setUserId(userId);
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, userId);
    Long dishId = shoppingCart.getDishId();
    if (dishId != null) {
      // 添加到购物车的是菜品
      queryWrapper.eq(ShoppingCart::getDishId, dishId);
    } else {
      queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
    }
    ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
    if (shoppingCart1.getNumber() > 1) {
      //菜品或套餐数量大于1，则减一
      shoppingCart1.setNumber(shoppingCart1.getNumber() - 1);
      shoppingCartService.updateById(shoppingCart1);
      return R.success(shoppingCart1);
    } else {
      //删除该菜品或套餐
      shoppingCartService.removeById(shoppingCart1);
      return R.success(shoppingCart1);
    }
  }
@DeleteMapping("/clean")
  public R<String> clean(){
  LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
  queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
  shoppingCartService.remove(queryWrapper);
  return R.success("清空购物车成功");
  }
}
