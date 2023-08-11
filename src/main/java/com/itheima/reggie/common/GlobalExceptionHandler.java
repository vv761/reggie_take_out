package com.itheima.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;


@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理异常
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> ExceptionHandler(SQLIntegrityConstraintViolationException ex){

        log.info(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            return R.error(s[2] + "已重复");
        }


        return R.error("未知错误");


    }

    /**
     * 处理异常
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> ExceptionHandler(CustomException ex){

        log.info(ex.getMessage());

        return R.error(ex.getMessage());


    }

}
