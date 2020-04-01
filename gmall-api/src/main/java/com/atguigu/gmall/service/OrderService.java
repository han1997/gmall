package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.OmsOrder;

public interface OrderService {
    String checkTradeCode(String memberId, String tradeCode);

    String getTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);
}
