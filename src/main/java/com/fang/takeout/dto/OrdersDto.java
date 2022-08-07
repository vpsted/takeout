package com.fang.takeout.dto;

import com.fang.takeout.entity.OrderDetail;
import com.fang.takeout.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

  private String userName;

  private String phone;

  private String address;

  private List<OrderDetail> orderDetails;

}
