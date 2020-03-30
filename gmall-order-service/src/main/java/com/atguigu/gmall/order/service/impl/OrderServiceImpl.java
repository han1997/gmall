package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.beans.OmsOrderItem;
import com.atguigu.gmall.manage.util.RedisUtil;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author hhy1997
 * 2020/3/29
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Reference
    CartService cartService;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
//            String tradeCodeFromCache = jedis.get(tradeKey);
//                jedis.del(tradeKey);
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
            if (eval != null && eval != 0) {
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }

        return "fail";
    }

    @Override
    public String getTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = "";
        try {
            jedis = redisUtil.getJedis();

            String tradeKey = "user:" + memberId + ":tradeCode";
            tradeCode = UUID.randomUUID().toString();
            if (jedis != null) {
                jedis.setex(tradeKey, 60 * 15, tradeCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }

        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
//        保存订单表
        omsOrderMapper.insertSelective(omsOrder);
//        保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItemMapper.insertSelective(omsOrderItem);
//            删除购物车内信息
        }


    }
}
