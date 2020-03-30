package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import util.HttpClientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/25
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*String newToken = request.getParameter("newToken");
        if(newToken!=null&&newToken.length()>0){
            CookieUtil.setCookie(request,response,"token",newToken,WebConst.cookieExpire,false);
        }
*/
        StringBuffer requestURL1 = request.getRequestURL();
        System.out.println(requestURL1);

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation == null) {
//            不需要进行登录拦截的方法，直接返回true
            return true;
        }
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
//        拦截成功后是否必须要登录成功
        boolean loginSuccess = methodAnnotation.loginSuccess();

        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            //            通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
            }
//            还是获取不到ip，给定一个默认值,项目应该直接返回，给出错误信息
            if (StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
//        调用认证中心验证token信息
            String successJson = HttpClientUtil.doGet("http://localhost:8085/verity?token=" + token + "&ip=" + ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }

        if (loginSuccess) {
//                验证
            if (!"success".equals(success)) {
//                    重定向回passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://localhost:8085/index?ReturnUrl=" + requestURL);
                return false;
            } else {
//                    写用户信息
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickName", successMap.get("nickName"));
            }
        } else {
//            即使不需要登录成功，也需要验证
            if ("success".equals(success)) {
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickName", successMap.get("nickName"));
            }
            else {
                return true;
            }
        }
//                    覆盖cookie,(防止cookie过期
        CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 72, true);
        return true;
    }
}
