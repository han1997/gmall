package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.beans.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/30
 */
@Controller
public class PaymentController {
    @Autowired
    AlipayClient alipayClient;
    @Reference
    PaymentService paymentService;
    @Reference
    OrderService orderService;

    @RequestMapping("/callback/return")
    @LoginRequired
    public String callbackReturn(HttpServletRequest request, ModelMap modelMap) {
// 回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();


        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)) {
            // 验签成功

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());
            // 更新用户的支付状态
            paymentService.updatePayment(paymentInfo);

        }

        return "finish";
    }


    @RequestMapping("/wx/submit")
    @LoginRequired
    @ResponseBody
    public String submit_wx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request,
                            ModelMap modelMap) {
        String sign = request.getParameter("sign");

        return null;
    }

    @RequestMapping("/alipay/submit")
    @LoginRequired
    @ResponseBody
    public String submit_ali(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request,
                             ModelMap modelMap) {
//        获得支付宝请求客户端
        String form = "";
        try {
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
// 回调函数
            alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
            alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

            Map<String, Object> map = new HashMap<>();
            map.put("out_trade_no", outTradeNo);
            map.put("product_code", "FAST_INSTANT_TRADE_PAY");
            map.put("total_amount", 0.01);
            map.put("subject", "尚硅谷感光徕卡Pro300瞎命名系列手机");

            String param = JSON.toJSONString(map);

            alipayRequest.setBizContent(param);
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

//        保存订单状态
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("谷粒商城商品一件");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);

//        提交请求到支付宝

//        发送消息检查订单状态
        paymentService.checkPaymentResultByDelayQueue(outTradeNo,5);

        return form;
    }

    @RequestMapping("/index")
    @LoginRequired
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request,
                        ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");
        modelMap.put("outTradeNo", outTradeNo);
        modelMap.put("totalAmount", totalAmount);
        modelMap.put("memberId", memberId);
        modelMap.put("nickName", nickName);
        return "index";
    }
}
