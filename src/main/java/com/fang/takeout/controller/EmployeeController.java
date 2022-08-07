package com.fang.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fang.takeout.common.R;
import com.fang.takeout.entity.Employee;
import com.fang.takeout.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController//@Controller + ReponseBody
@RequestMapping("/employee")//RESTful风格
public class EmployeeController {
  @Autowired
  private EmployeeService employeeService;

  /**
   * 员工登录
   *
   * @param request
   * @param employee 前端传来的员工数据
   * @return
   */
  //替代了@RequestMapping(method = RequestMethod.POST)
  @PostMapping("/login")
  public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {//@RequestBody说明参数json形式
    //1、将页面提交的密码password进行md5加密处理
    String password = employee.getPassword();
    password = DigestUtils.md5DigestAsHex(password.getBytes());

    //2、根据页面提交的用户名username查询数据库
    LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Employee::getUsername, employee.getUsername());
    Employee emp = employeeService.getOne(queryWrapper);

    //3、如果没有查询到则返回登录失败结果
    if (emp == null) {
      return R.error("登录失败");
    }

    //4、密码比对，如果不一致则返回登录失败结果
    if (!emp.getPassword().equals(password)) {
      return R.error("登录失败");
    }

    //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
    if (emp.getStatus() == 0) {
      return R.error("账号已禁用");
    }

    //6、登录成功，将员工id存入Session并返回登录成功结果
    request.getSession().setAttribute("employee", emp.getId());
    return R.success(emp);
  }

  /**
   * 员工退出
   *
   * @param request
   * @return
   */
  @PostMapping("/logout")
  public R<String> logout(HttpServletRequest request) {
    //清除本地的对应session
    request.getSession().removeAttribute("employee");
    return R.success("退出成功");
  }

  @PostMapping//请求路径为/employee，RESTful风格
  public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
    //设置初始密码123456，进行md5加密
    employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

    /*由公共字段填充函数实现
    employee.setCreateTime(LocalDateTime.now());
    employee.setUpdateTime(LocalDateTime.now());
    Long empId = (Long) request.getSession().getAttribute("employee");
    employee.setCreateUser(empId);
    employee.setUpdateUser(empId);
    */

    //employeeService的save方法在EmployeeService接口的父接口IService定义
    employeeService.save(employee);
    return R.success("添加员工成功");
  }

  /**
   * 员工信息分页查询
   *
   * @param page
   * @param pageSize
   * @return
   */
  @GetMapping("/page")
  public R<Page<Employee>> pageR(int page, int pageSize, String name) {
    log.info("page={},pageSize={},name={}", page, pageSize, name);
    //分页构造器
    Page<Employee> pageInfo = new Page(page, pageSize);
    //条件构造器
    LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

    //添加过滤条件,like查询是使用的默认方式，也就是说在查询条件的左右两边都有%：NAME = ‘%王%'；
    // 如果只需要在左边或者右边拼接%，可以使用likeLeft或者likeRight方法
    queryWrapper.like(StringUtils.hasText(name), Employee::getName, name);

    //添加排序条件
    queryWrapper.orderByDesc(Employee::getUpdateTime);
    //page方法回去调用BaseMapper接口的方法
    employeeService.page(pageInfo, queryWrapper);
    return R.success(pageInfo);
  }

  @PutMapping
  public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
    /*由公共字段填充函数实现
    Long empId = (Long) request.getSession().getAttribute("employee");
    employee.setUpdateUser(empId);
    employee.setUpdateTime(LocalDateTime.now());
    */

    employeeService.updateById(employee);
    return R.success("员工信息修改成功");

  }

  @GetMapping("/{id}")
  public R<Employee> getById(@PathVariable Long id) {
    log.info("查询员工消息");
    Employee employee = employeeService.getById(id);
    if (employee != null) {
      return R.success(employee);
    }
    return R.error("查询失败");
  }

}

