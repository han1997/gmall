package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.beans.PaymentInfo;
import com.atguigu.gmall.user.mq.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/30
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    AlipayClient alipayClient;

    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
//      进行幂等性检查
        PaymentInfo t = new PaymentInfo();
        t.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoFromDb = paymentInfoMapper.selectOne(t);
        String paymentStatus = paymentInfoFromDb.getPaymentStatus();
        if (StringUtils.isNotBlank(paymentStatus) && "已支付".equals(paymentStatus)){
            return;
        }

        Example o = new Example(PaymentInfo.class);
        o.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            paymentInfoMapper.updateByExampleSelective(paymentInfo, o);
            //        支付成功后引起其他系统服务
//            发送订单已支付的消息
            Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());

            producer.send(mapMessage);
            session.commit();
        } catch (Exception e) {
//            消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void checkPaymentResultByDelayQueue(String outTradeNo, int count) {

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_check_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_check_queue);
            ActiveMQMapMessage message = new ActiveMQMapMessage();
            message.setString("out_trade_no", outTradeNo);
            message.setString("count", String.valueOf(count));
//            设置消息延迟
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 60);

            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String outTradeNo) {
        HashMap<String, Object> resultMap = new HashMap<>();
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", outTradeNo);
        alipayRequest.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(alipayRequest);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("有可能交易已创建，调用成功");

            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
            resultMap.put("call_back_content", response.getMsg());
        } else {
            System.out.println("有可能交易未创建，调用失败");
        }
        return resultMap;
    }
}
