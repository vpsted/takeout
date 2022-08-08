package com.fang.takeout.filter;

import com.alibaba.fastjson.JSON;
import com.fang.takeout.common.BaseContext;
import com.fang.takeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 拦截资源请求，判断是否登录
 */
//过滤器可以把对资源的请求拦截下来，从而实现一些特殊的功能,/*表示拦截所有请求
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
  //路径匹配器，用来匹配路径
  public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    //1.获取本次请求的URI
    String requestURI = request.getRequestURI();
    //放行的URI
    String[] uris = new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/common/**",
            "/user/sendMsg",
            "/user/login",
            "/user/loginout",
            //swagger相关
            "/doc.html",
            "/webjars/**",
            "/swagger-resources",
            "/v2/api-docs"
    };

    //2.判断本次请求是否需要放行
    boolean checkRes = check(uris, requestURI);

    //3.直接放行
    if (checkRes) {
      filterChain.doFilter(request, response);
      return;
    }

    //4.判断员工登录状态

    if (request.getSession().getAttribute("employee") != null) {
      Long empId = (Long) request.getSession().getAttribute("employee");
      BaseContext.setCurrentId(empId);
      filterChain.doFilter(request, response);
      return;
    }
    //4.判断登录状态
    if (request.getSession().getAttribute("user") != null) {
      Long userId = (Long) request.getSession().getAttribute("user");
      BaseContext.setCurrentId(userId);
      filterChain.doFilter(request, response);
      return;
    }

//    log.info("拦截请求：{}", request.getRequestURI());//{}表示后面参数的占位

    //5.如果未登录，返回未登录结果，通过输出流的方式向客户端页面响应数据
    response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

  }

  /**
   * 路径匹配，判断本次请求是否需要放行
   *
   * @param uris       放行uri
   * @param requestURI 本次请求uri
   * @return true放行 false不放行
   */
  public boolean check(String[] uris, String requestURI) {
    for (String uri : uris) {
      boolean match = PATH_MATCHER.match(uri, requestURI);
      if (match) {
        return true;
      }
    }
    return false;
  }

}

