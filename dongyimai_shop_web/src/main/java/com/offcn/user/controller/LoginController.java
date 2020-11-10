package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {
    @RequestMapping("showLoginName")
    public Map showLoginName(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName",userName);
        return map;
    }
}
