package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.annotations.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hhy1997
 * 2020/3/30
 */
@Controller
public class PaymentController {

    @RequestMapping("/index")
    @LoginRequired
    public String index(String outTradeNo, String totalAmount, HttpServletRequest request,
                      ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("memberId",memberId);
        modelMap.put("nickName",nickName);

        return "index";
    }
}
