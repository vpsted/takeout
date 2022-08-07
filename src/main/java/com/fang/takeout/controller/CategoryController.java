package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fang.takeout.common.R;
import com.fang.takeout.entity.Category;
import com.fang.takeout.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//@Controller + ReponseBody
@RequestMapping("/category")
@Slf4j
public class CategoryController {
  @Autowired
  private CategoryService categoryService;

  @PostMapping
  public R<String> save(@RequestBody Category category) {
    categoryService.save(category);
    return R.success("添加分类成功");
  }

  @GetMapping("/page")
  public R<Page> page(int page, int pageSize) {
    Page<Category> pageInfo = new Page<>(page, pageSize);
    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.orderByAsc(Category::getSort);
    categoryService.page(pageInfo, queryWrapper);
    return R.success(pageInfo);
  }

  @DeleteMapping
  public R<String> delete(Long id) {
//    categoryService.removeById(id);
    categoryService.remove(id);
    return R.success("删除分类成功");
  }

  @PutMapping
  public R<String> update(@RequestBody Category category) {
    categoryService.updateById(category);
    return R.success("修改分类信息成功");
  }

  @GetMapping("/list")
  public R<List<Category>> list(Category category) {
    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
    if(category.getType()!=null){
      queryWrapper.eq(Category::getType, category.getType());
    }
    queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
    List<Category> list = categoryService.list(queryWrapper);
    return R.success(list);
  }
}
