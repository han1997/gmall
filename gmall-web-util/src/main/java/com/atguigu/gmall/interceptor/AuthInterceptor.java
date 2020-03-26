package com.atguigu.gmall.interceptor;

import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import util.HttpClientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

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
        String success = "";
        if (StringUtils.isNotBlank(token)){
//        调用认证中心验证token信息
            success = HttpClientUtil.doGet("http:localshot:8085/verity?tiken=" + token);
        }


//        拦截成功后是否必须要登录成功
        boolean loginSuccess = methodAnnotation.loginSuccess();
        if (loginSuccess) {
            if (StringUtils.isBlank(token)) {
//                踢回认证中心
            }else {
//                验证
                if (!"success".equals(success)){
//                    重定向回passport登录
                    StringBuffer requestURL = request.getRequestURL();
                    response.sendRedirect("http:localshot:8085/index?ReturnUrl=" + requestURL);
                    return false;
                }else {
//                    写用户信息
                    request.setAttribute("memberId","1");
                    request.setAttribute("nickName","test小明");
                }
            }
        }else {
//            即使不需要登录成功，也需要验证
            if ("success".equals(success)){
                request.setAttribute("memberId","1");
                request.setAttribute("nickName","test小明");
            }
        }
//                    覆盖cookie,(防止cookie过期
        CookieUtil.setCookie(request,response,"oldToken",token,60*60*72,true);
        return true;
    }
}
