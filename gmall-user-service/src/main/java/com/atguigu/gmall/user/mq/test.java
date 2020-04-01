package com.atguigu.gmall.user.mq;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.*;

/**
 * @author hhy1997
 * 2020/4/1
 */
@RestController
public class test {
    @Autowired
    ActiveMQUtil activeMQUtil;

    @RequestMapping("/testMQ")
    public String testMQ(){
        String message = "";
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue test = session.createQueue("hahaha");
            MessageProducer producer = session.createProducer(test);
            ActiveMQTextMessage message1 = new ActiveMQTextMessage();
            message1.setText("我来啦!");
            producer.send(message1);
            message = "success";

        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            message = "fail";
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return message;
    }
}
