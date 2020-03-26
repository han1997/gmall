package com.atguigu.gmall.passport.controller;

import com.atguigu.gmall.bean.UmsMember;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hhy1997
 * 2020/3/25
 */
@Controller
public class IndexController {
    @RequestMapping("/verity")
    @ResponseBody
    public String verity(String token){

        return "success";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember){
        String token = "";

        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }
}
