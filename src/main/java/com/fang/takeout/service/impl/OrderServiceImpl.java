package com.fang.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fang.takeout.common.BaseContext;
import com.fang.takeout.common.CustomException;
import com.fang.takeout.entity.*;
import com.fang.takeout.mapper.OrderMapper;
import com.fang.takeout.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
  @Autowired
  private ShoppingCartService shoppingCartService;
  @Autowired
  private UserService userService;
  @Autowired
  private AddressBookService addressBookService;
  @Autowired
  private OrderDetailService orderDetailService;

  @Override
  @Transactional
  public void submit(Orders orders) {
    //获取用户id
    Long userId = BaseContext.getCurrentId();
    //查询购物车数据
    LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
    shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);

    List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
    if (shoppingCartList == null || shoppingCartList.isEmpty()) {
      throw new CustomException("购物车为空");
    }
    //查询用户信息
    User user = userService.getById(userId);
    //查询地址信息
    AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
    if (addressBook == null) {
      throw new CustomException("地址为空");
    }
    //amount用于计算总金额
    AtomicInteger amount = new AtomicInteger(0);
    //生成订单号
    long orderId = IdWorker.getId();

    //生成订单明细数据
    List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
      OrderDetail orderDetail = new OrderDetail();
      orderDetail.setOrderId(orderId);
      orderDetail.setNumber(item.getNumber());
      orderDetail.setDishFlavor(item.getDishFlavor());
      orderDetail.setDishId(item.getDishId());
      orderDetail.setSetmealId(item.getSetmealId());
      orderDetail.setName(item.getName());
      orderDetail.setImage(item.getImage());
      orderDetail.setAmount(item.getAmount());
      amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
      return orderDetail;
    }).collect(Collectors.toList());
    // 向订单明细表插入购物车的多条数据
    orderDetailService.saveBatch(orderDetails);

    //向生成订单数据
    orders.setId(orderId);
    orders.setOrderTime(LocalDateTime.now());
    orders.setCheckoutTime(LocalDateTime.now());
    orders.setStatus(2);
    orders.setAmount(new BigDecimal(amount.get()));//总金额
    orders.setUserId(userId);
    orders.setNumber(String.valueOf(orderId));
    orders.setUserName(user.getName());
    orders.setConsignee(addressBook.getConsignee());
    orders.setPhone(addressBook.getPhone());
    orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
            + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
            + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
            + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
    //向订单表插入一条记录
    this.save(orders);


  }
}