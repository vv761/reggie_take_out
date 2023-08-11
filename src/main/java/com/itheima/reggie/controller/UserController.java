package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.util.SMSUtils;
import com.itheima.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * 发送手机短信验证码
     *
     * @param user
     * @param httpSession
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession) {
        //获取手机号
        String phone = user.getPhone();

        if (phone!=null) {
            //生成一个四位随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code:{}", code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("庆庆外卖","",phone,code);

            //需要将生成的验证码保存到Session
            httpSession.setAttribute(phone, code);

            return R.success("手机验证码发送成功");
        }
        return R.error("短信发送失败");
    }

    @PostMapping("/login")
    public R<User> sendMsg(@RequestBody Map map, HttpSession httpSession) {
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获得保存的验证码
        Object codeInSession = httpSession.getAttribute(phone);

        //进行验证码的比对（页面提交的验证码与session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果能比对成功，则表示登陆成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);


            User user = userService.getOne(queryWrapper);

            if (user == null){
                //如果为空则表示是新用户,直接注册登录
                user =new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);

            }
            httpSession.setAttribute("user",user.getId());
            return  R.success(user);

        }

        return R.error("登录失败");


    }
}
