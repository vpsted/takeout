package com.fang.takeout.common;

/**
 * 基于ThreadLocal工具类，用户保存和获取当前登录id
 */
public class BaseContext {
  //不同线程的ThreadLocal是不同的
  private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

  public static void setCurrentId(Long id) {
    threadLocal.set(id);
  }

  public static Long getCurrentId() {
    return threadLocal.get();
  }
}
