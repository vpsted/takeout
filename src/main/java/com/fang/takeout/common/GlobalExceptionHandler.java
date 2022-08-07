package com.fang.takeout.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 * annotations指定需要处理的类型
 * ResponseBody注解，因为有json数据返回
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
  /**
   * 异常处理方法
   *
   * @param e
   * @return
   */
  @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
  public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e) {
    if (e.getMessage().contains("Duplicate entry")) {
      //分离错误信息
      String[] split = e.getMessage().split(" ");
      String msg = split[2] + "已存在";
      return R.error(msg);
    }
    return R.error("未知错误");
  }

  /**
   * 异常处理方法
   * @param e 自定义异常
   * @return
   */
  @ExceptionHandler(CustomException.class)
  public R<String> exceptionHandler(CustomException e) {
    return R.error(e.getMessage());
  }


}
