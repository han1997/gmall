package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/25
 */
@Controller
public class IndexController {
    @Reference
    UserService userService;

    @RequestMapping("/vlogin")
    public void vlogin(){

    }

    @RequestMapping("/verity")
    @ResponseBody
    public String verity(String token, String ip) {

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall0115", ip);
        Map<String, String> map = new HashMap<>();
        if (decode != null) {
            map.put("status","success");
            map.put("memberId", String.valueOf(decode.get("memberId")));
            map.put("nickName", String.valueOf(decode.get("nickName")));
        }else {
            map.put("status","fail");
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        UmsMember umsMemberLogin = userService.login(umsMember);
        String token = "";
        if (umsMemberLogin != null) {
//            登录成功
            Map<String,Object> map = new HashMap<>();
            Long memberId = umsMemberLogin.getId();
            map.put("memberId", memberId);
            map.put("nickName",umsMemberLogin.getNickname());

//            通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
            }
//            还是获取不到ip，给定一个默认值,项目应该直接返回，给出错误信息
            if (StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }

//            制作token
            token = JwtUtil.encode("2019gmall0115", map, ip);
//            存入redis
            userService.addUserToken(token, memberId);

//            登录失败

        }

        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap) {
        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }
}
