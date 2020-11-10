package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {


    //获取当前登录用户账号
    @RequestMapping("/showLoginName")
    @ResponseBody
    public Map showLoginName(){
        //获取springSecurity的上下文环境，获取当前登录用户账号
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();

        map.put("userName",userName);

        return map;
        }


}
