package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fang.takeout.common.R;
import com.fang.takeout.entity.User;
import com.fang.takeout.service.UserService;
import com.fang.takeout.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
  @Autowired
  private UserService userService;
  @Autowired
  private RedisTemplate redisTemplate;

  /**
   * 发送短信验证码
   *
   * @param user
   * @return
   */
  @PostMapping("/sendMsg")
  public R<String> sendMsg(@RequestBody User user) {
    String phone = user.getPhone();
    if (StringUtils.hasText(phone)) {
      String validateCode = ValidateCodeUtils.generateValidateCode(4).toString();
      log.info("验证码为{}", validateCode);
      // SMSUtils.sendMessage("外卖","",phone,validateCode);

      //将验证码保存到redis中以便后续比对
      redisTemplate.opsForValue().set(phone,validateCode,5, TimeUnit.MINUTES);
      return R.success("短信发送成功");
    }
    return R.error("短信发送失败");
  }

  @PostMapping("/login")
  public R<User> login(@RequestBody Map map, HttpSession session) {
    String phone = map.get("phone").toString();
    String code = map.get("code").toString();
    String codeInRedis = (String) redisTemplate.opsForValue().get(phone);
    if (code.equals(codeInRedis)) {
      //判断用户是否注册，如未注册则进行注册
      LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
      lambdaQueryWrapper.eq(User::getPhone, phone);
      User user = userService.getOne(lambdaQueryWrapper);
      if (user == null) {
        user = new User();
        user.setPhone(phone);
        user.setStatus(1);
        userService.save(user);
      }
      //LoginCheckFilter会根据session判断用户是否登录进行过滤
      session.setAttribute("user", user.getId());
      redisTemplate.delete(phone);
      return R.success(user);
    }
    return R.error("登录失败");
  }

  /**
   * 用户退出
   *
   * @param request
   * @return
   */
  @PostMapping("/loginout")
  public R<String> logout(HttpServletRequest request) {
    //清除本地的对应session
    request.getSession().removeAttribute("user");
    return R.success("退出成功");
  }
}
