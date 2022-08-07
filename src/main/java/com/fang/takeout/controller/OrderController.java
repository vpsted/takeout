package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fang.takeout.common.BaseContext;
import com.fang.takeout.common.R;
import com.fang.takeout.dto.OrdersDto;
import com.fang.takeout.entity.OrderDetail;
import com.fang.takeout.entity.Orders;
import com.fang.takeout.entity.ShoppingCart;
import com.fang.takeout.service.OrderDetailService;
import com.fang.takeout.service.OrderService;
import com.fang.takeout.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/order")
public class OrderController {
  @Autowired
  private OrderService orderService;
  @Autowired
  private OrderDetailService orderDetailService;
  @Autowired
  private ShoppingCartService shoppingCartService;

  /**
   * 下单
   *
   * @param orders
   * @return
   */
  @PostMapping("/submit")
  public R<String> submit(@RequestBody Orders orders) {
    orderService.submit(orders);
    //清空购物车
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
    shoppingCartService.remove(queryWrapper);

    return R.success("下单成功");
  }

  /**
   * 订单以及关联订单详情分页查询，用于用户端查询
   *
   * @param page
   * @param pageSize
   * @return
   */
  @GetMapping("/userPage")
  public R<Page<OrdersDto>> userPage(int page, int pageSize) {
    Page<Orders> orderPage = new Page<>(page, pageSize);
    LambdaQueryWrapper<Orders> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
    orderLambdaQueryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
    orderLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
    orderService.page(orderPage, orderLambdaQueryWrapper);
    Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
    BeanUtils.copyProperties(orderPage, ordersDtoPage, "records");

    List<OrdersDto> ordersDtoList = orderPage.getRecords().stream().map(orders -> {
      OrdersDto ordersDto = new OrdersDto();
      BeanUtils.copyProperties(orders, ordersDto);
      LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
      orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, orders.getId());
      List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);
      ordersDto.setOrderDetails(orderDetailList);
      return ordersDto;
    }).collect(Collectors.toList());
    ordersDtoPage.setRecords(ordersDtoList);
    return R.success(ordersDtoPage);
  }

  /**
   * 订单分页查询，用于管理端
   *
   * @param page
   * @param pageSize
   * @param number
   * @param beginTime
   * @param endTime
   * @return
   */
  @GetMapping("/page")
  public R<Page<Orders>> employeePage(int page, int pageSize, String number,
                                      @DateTimeFormat String beginTime,@DateTimeFormat String endTime) {
    LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
    Page<Orders> ordersPage = new Page<>(page, pageSize);
    ordersLambdaQueryWrapper.like(number != null, Orders::getId, number);
    ordersLambdaQueryWrapper.between(beginTime != null && endTime != null,
            Orders::getOrderTime, beginTime, endTime);
    ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
    orderService.page(ordersPage, ordersLambdaQueryWrapper);
    return R.success(ordersPage);
  }
  /**
   * 更新订单，设置订单派送状态
   * @param orders
   * @return
   */
  @PutMapping
  public R<String> update(@RequestBody Orders orders) {
    Stream<String> stream = Stream.of("张三丰", "张翠山", "赵敏", "周芷若", "张无忌");
    //对Stream流中的元素进行过滤,只要姓张的人
    Stream<String> stream2 = stream.filter((String name)->{return name.startsWith("张");});
    LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
    ordersLambdaQueryWrapper.eq(Orders::getId, orders.getId());
    Orders newOrder = orderService.getOne(ordersLambdaQueryWrapper);
    newOrder.setStatus(orders.getStatus());
    orderService.updateById(newOrder);
    return R.success("更新订单成功");
  }
}
