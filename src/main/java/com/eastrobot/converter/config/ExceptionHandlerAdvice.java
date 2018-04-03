/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.converter.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * 配置全局异常
 *
 * @author Yogurt_lei
 * @date 2018-04-03 11:10
 */
@ControllerAdvice
public class ExceptionHandlerAdvice {

    //全局异常处理
    @ExceptionHandler(value = Exception.class)
    public ModelAndView exception(Exception e, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    //预处理前台请求参数
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {

    }
}
