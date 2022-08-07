package com.fang.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fang.takeout.entity.Employee;
import com.fang.takeout.mapper.EmployeeMapper;
import com.fang.takeout.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service//由spring管理
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {
}
