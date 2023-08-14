package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器,支持通配符
    public static  final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取本次请求的URL
       String requestURL = request.getRequestURI();
       log.info("拦截到请求{}",requestURL);
       //定义不需要处理的路径
       String[] urls = new String[]{
               "/employee/login",
               "/employee/logout",
               "/backend/**",
               "/front/**",
               "/common/**",
               "/user/sendMsg", // 移动端发送短信
               "/user/login" ,// 移动端登录
               "/dpc.html",
               "/webjars/**",
               "swagger-resources",
               "/v2/api-docs"
       };

       //判断本次请求是否需要处理
        boolean check = check(requestURL, urls);

        //如果不需要处理，则直接放行
        if (check){
            log.info("本次请求{}不需要处理",requestURL);
            filterChain.doFilter(request,response);
            return;
        }

        //判断员工是否已经登录，如果已登录，也是直接放行
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为:{}",request.getSession().getAttribute("employee"));
            Long emdId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(emdId);
            filterChain.doFilter(request,response);
            return;
        }
        //判断用户是否已经登录，如果已登录，也是直接放行
        if (request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为:{}",request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }

        //如果未登录则返回未登录结果，通过输出流像页面客户端页面相应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }



    /**
     * 路径匹配，检查本次路径是否可放行
     * @param requestURL
     * @param urls
     * @return
     */
    public  boolean check(String requestURL,String[] urls){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match){
                return true;
            }
        }
        return false;
    }
}
