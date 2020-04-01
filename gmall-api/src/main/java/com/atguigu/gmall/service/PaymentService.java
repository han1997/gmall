package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void checkPaymentResultByDelayQueue(String outTradeNo, int count);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}
