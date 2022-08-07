package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fang.takeout.common.BaseContext;
import com.fang.takeout.common.R;
import com.fang.takeout.entity.AddressBook;
import com.fang.takeout.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

  @Autowired
  private AddressBookService addressBookService;

  /**
   * 新增地址
   */
  @PostMapping
  public R<AddressBook> save(@RequestBody AddressBook addressBook) {
    //获取用户id并添加
    addressBook.setUserId(BaseContext.getCurrentId());
    addressBookService.save(addressBook);
    return R.success(addressBook);
  }

  /**
   * 设置默认地址
   */
  @PutMapping("default")
  public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
    LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
    //查找属于当前用户的地址
    wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
    //全部设为非默认地址
    wrapper.set(AddressBook::getIsDefault, 0);
    addressBookService.update(wrapper);
    //将当前选中的地址设为默认地址
    addressBook.setIsDefault(1);
    addressBookService.updateById(addressBook);
    return R.success(addressBook);
  }

  /**
   * 根据地址的id查询地址
   */
  @GetMapping("/{id}")
  public R<AddressBook> get(@PathVariable Long id) {
    AddressBook addressBook = addressBookService.getById(id);
    if (addressBook != null) {
      return R.success(addressBook);
    } else {
      return R.error("没有找到该地址");
    }
  }

  /**
   * 查询默认地址
   */
  @GetMapping("default")
  public R<AddressBook> getDefault() {
    LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
    queryWrapper.eq(AddressBook::getIsDefault, 1);

    AddressBook addressBook = addressBookService.getOne(queryWrapper);

    if (null == addressBook) {
      return R.error("没有找到该地址");
    } else {
      return R.success(addressBook);
    }
  }

  /**
   * 查询指定用户的全部地址
   */
  @GetMapping("/list")
  public R<List<AddressBook>> list(AddressBook addressBook) {
    addressBook.setUserId(BaseContext.getCurrentId());
    LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
    queryWrapper.orderByDesc(AddressBook::getUpdateTime);
    return R.success(addressBookService.list(queryWrapper));
  }

  /**
   * 修改地址
   *
   * @param addressBook
   * @return
   */
  @PutMapping
  public R<String> update(@RequestBody AddressBook addressBook) {
    if (addressBookService.updateById(addressBook) == true) {
      return R.success("修改地址成功");
    }
    return R.error("修改地址失败");
  }

  @DeleteMapping
  public R<String> delete(@RequestParam Long ids) {
    if (addressBookService.removeById(ids) == true) {
      return R.success("删除地址成功");
    }
    return R.success("删除地址失败");
  }
}
