package com.fang.takeout.config;

import com.fang.takeout.common.JacksonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

//todome 2放行静态资源
@Slf4j
@Configuration//添加注解，使其能被识别为配置类
public class WebMvcConfig extends WebMvcConfigurationSupport {
  /**
   * 设置静态资源映射，对静态资源进行放行
   *
   * @param registry
   */
  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    log.info("进行静态资源映射");
//    将前者映射到后者，classpath指的是java和resources两个文件夹
    registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
    registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
  }

  /**
   * MVC原本由默认转换器（转换器将将返回的R对象转换为json数据）
   * 扩展MVC的消息锡转换器，转换器将
   *
   * @param converters
   */
  @Override
  protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    //创建消息转换器对象
    MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
    //设置转换器对象，底层使用Jackson将Java对象转为Json
    messageConverter.setObjectMapper(new JacksonObjectMapper());
    //将上面的消息转换器对象追加到MVC框架的转换器集合中，放在转换器集合的最前面（即默认集合前）
    converters.add(0, messageConverter);
    //super.extendMessageConverters(converters);
  }

}
