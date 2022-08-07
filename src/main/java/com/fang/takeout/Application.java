package com.fang.takeout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//todome 1.创建SpringBoot启动类
@Slf4j//支持log打印日志
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    log.info("项目启动成功");
  }
}
