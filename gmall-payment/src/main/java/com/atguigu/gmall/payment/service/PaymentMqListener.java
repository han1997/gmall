package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.beans.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/4/1
 */
@Component
public class PaymentMqListener {
    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_CHECK_QUEUE")
    public void consumePaymentResult(MapMessage mapMessage) {
        String out_trade_no = null;
        int count = 0;
        try {
            out_trade_no = mapMessage.getString("out_trade_no");
            count = Integer.parseInt(mapMessage.getString("count"));
            if (count > 0) {
//            调用接口查询订单状态
                Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);
                if (resultMap != null || !resultMap.isEmpty()) {
                    String trade_status = (String) resultMap.get("trade_status");
                    if (StringUtils.isNotBlank(trade_status) && "TRADE_SUCCESS".equals(trade_status)) {
                        //                支付成功，调用更新支付
                        System.out.println("支付成功");

                        PaymentInfo paymentInfo = new PaymentInfo();
                        paymentInfo.setOrderSn(out_trade_no);
                        paymentInfo.setPaymentStatus("已支付");
                        paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));// 支付宝的交易凭证号
                        paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));//回调请求字符串
                        paymentInfo.setCallbackTime(new Date());
                        // 更新用户的支付状态
                        paymentService.updatePayment(paymentInfo);

                        return;
                    } else {
//                        未支付成功
                    }
                }
                count--;
                System.out.println("未支付成功！剩余检查次数：" + count + "，继续发送延迟检查");
                paymentService.checkPaymentResultByDelayQueue(out_trade_no, count);
            } else {
                System.out.println("查询已达5次，放弃查询");
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
