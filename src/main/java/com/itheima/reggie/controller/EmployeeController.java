package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.naming.Name;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将密码进行md5处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());


        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //如果没有查到
        if (emp == null){
            return  R.error("登录失败");
        }

        //密码比对
        if (!(emp.getPassword().equals(password))){
            return  R.error("登录失败");
        }

        //查看员工是否被禁用
        if (emp.getStatus() == 0){
            return  R.error("账号已禁用");
        }


        //账号没有异常登陆成功
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);


    }
    /**
     * 退出功能
     */
    @PostMapping("/logout")
    public  R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，信息为:{}",employee.toString());

        //初始化密码，并进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //创建时间和更新时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        //Long empID = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empID);
        //employee.setUpdateUser(empID);

        employeeService.save(employee);
        
        return R.success("新增员工成功");
    }

    /**
     * 分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();

        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName, name);

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);



        return R.success(pageInfo);


    }

    /**
     * 禁用员工
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //Long empID = (Long) request.getSession().getAttribute("employee");
        log.info(employee.toString());
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empID);
        employeeService.updateById(employee);
        return R.success("员工消息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){

        log.info("根据id查询员工信息");
        Employee byId = employeeService.getById(id);
        if (byId != null) {
            return R.success(byId);
        }
        return R.error("员工信息为空");
    }
}
