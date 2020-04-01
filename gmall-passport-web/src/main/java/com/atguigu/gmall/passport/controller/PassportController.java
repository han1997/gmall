package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import util.HttpClientUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/25
 */
@Controller
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("toVLogin")
    public String a() {
        String url = "api.weibo.com/oauth2/authorize?client_id=1565861648&response_type=code&redirect_uri=http://127.0.0.1:8085/vlogin";
        return "redirect:https://" + url;
    }

    @RequestMapping("/vlogin")
    public String vlogin(String code, HttpServletRequest request) {
        String token = "";
        String accessToken = "";
        String uid = "";
//        使用code换取access_token
        String s2 = "https://api.weibo.com/oauth2/access_token";
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "1565861648");
        map.put("client_secret", "d46d195cf435d955f4e7cce42f7a476e");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://127.0.0.1:8085/vlogin");
        map.put("code", code);
        String accessTokenJson = HttpClientUtil.doPost(s2, map);
        Map<String, String> accessTokenMap = JSON.parseObject(accessTokenJson, Map.class);
        Map userInfoMap = null;
//        使用access_token获取用户信息
        if (accessTokenMap != null && accessTokenMap.size() > 0) {
            accessToken = accessTokenMap.get("access_token");
            uid = (String) accessTokenMap.get("uid");
            if (StringUtils.isNotBlank(accessToken)) {
                String userInfoJson = HttpClientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=" + accessToken + "&uid=" + uid);
                if (StringUtils.isNotBlank(userInfoJson)) {
                    userInfoMap = JSON.parseObject(userInfoJson, Map.class);
                }
            }
        }
//        将用户信息存储到数据库,用户类型设置为微博用户
        if (userInfoMap != null && userInfoMap.size() > 0) {
            UmsMember umsMember = new UmsMember();
            umsMember.setSourceType("2");
            umsMember.setAccessToken(accessToken);
            umsMember.setAccessCode(code);
            umsMember.setSourceUid((String) userInfoMap.get("idstr"));
            umsMember.setCity((String) userInfoMap.get("location"));
            String gender = (String) userInfoMap.get("gender");
            String g = "0";
            if ("m".equals(gender)) {
                g = "1";
            }
            umsMember.setGender(g);
            umsMember.setNickname((String) userInfoMap.get("screen_name"));
            UmsMember checkUser = new UmsMember();
            checkUser.setSourceUid(umsMember.getSourceUid());
            UmsMember checkMemberFromDb = userService.checkOauthUser(checkUser);
            if (checkMemberFromDb == null) {
                umsMember = userService.addOauthUser(umsMember);
            } else {
                umsMember = checkMemberFromDb;
            }
            String memberId = umsMember.getId();
            String nickname = umsMember.getNickname();

//        生成jwt的token ，重定向回首页
            token = getJwtToken(request, memberId, nickname);
            //            存入redis
            userService.addUserToken(token, memberId);
        }

        return "redirect:http://127.0.0.1:8083/index?token=" + token;
    }

    @RequestMapping("/verity")
    @ResponseBody
    public String verity(String token, String ip, HttpServletRequest request) {

        StringBuffer requestURL = request.getRequestURL();
        System.out.println("verity-->url:" + requestURL + "ip:" + ip + "token:" + token);

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall0115", ip);
        Map<String, String> map = new HashMap<>();
        if (decode != null) {
            map.put("status", "success");
            map.put("memberId", String.valueOf(decode.get("memberId")));
            map.put("nickName", String.valueOf(decode.get("nickName")));
        } else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        UmsMember umsMemberLogin = null;
        if (umsMember != null) {
            umsMemberLogin = userService.login(umsMember);
        }
        String token = "";
        if (umsMemberLogin != null) {
//            登录成功
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            token = getJwtToken(request, memberId, nickname);
            System.out.println("login-->token:" + token);
//            存入redis
            userService.addUserToken(token, memberId);


        } else {
//            登录失败
            token = "fail";
        }

        return token;
    }

    private String getJwtToken(HttpServletRequest request, String memberId, String nickname) {
        String token;
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", memberId);
        map.put("nickName", nickname);

//            通过nginx转发的客户端ip
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();
        }
//            还是获取不到ip，给定一个默认值,项目应该直接返回，给出错误信息
        if (StringUtils.isBlank(ip)) {
            ip = "127.0.0.1";
        }

//            制作token
        token = JwtUtil.encode("2019gmall0115", map, ip);
        return token;
    }

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = false)
    public String index(String returnUrl, ModelMap modelMap, HttpServletRequest request) {
        StringBuffer requestURI = request.getRequestURL();
        modelMap.put("returnUrl"
                , returnUrl);
        return "index";
    }
}
