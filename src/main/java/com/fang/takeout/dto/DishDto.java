package com.fang.takeout.dto;

import com.fang.takeout.entity.Dish;
import com.fang.takeout.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
  //菜品对应的口味
  private List<DishFlavor> flavors = new ArrayList<>();
  //菜品对应的菜品类名称
  private String categoryName;
  //菜品数量
  private Integer copies;
}
