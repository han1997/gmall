package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.IUserService;
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
    @Reference
    private IUserService userService;

    @RequestMapping("/")
    @ResponseBody
    public List<UmsMember> index() {
        List<UmsMember> users = userService.list();
        return users;
    }
}
