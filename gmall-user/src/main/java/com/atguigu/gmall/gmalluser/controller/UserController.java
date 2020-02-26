package com.atguigu.gmall.gmalluser.controller;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.gmalluser.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/22
 */
@Controller
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @RequestMapping("/")
    @ResponseBody
    public List<UmsMember> index() {
        List<UmsMember> users = userService.list();
        return users;
    }
}
