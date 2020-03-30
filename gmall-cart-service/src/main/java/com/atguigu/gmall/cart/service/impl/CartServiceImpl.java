package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.manage.util.RedisUtil;
import com.atguigu.gmall.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author hhy1997
 * 2020/3/24
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem t = new OmsCartItem();
        t.setMemberId(memberId);
        t.setProductSkuId(skuId);
        OmsCartItem omsCartItem = omsCartItemMapper.selectOne(t);
        return omsCartItem;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);
        }

    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDB) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id",omsCartItemFromDB.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDB, example);

    }

    @Override
    public void flushCartCache(String memberId) {
//        获取数据库购物车信息
        OmsCartItem t = new OmsCartItem();
        t.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(t);

        Jedis jedis = redisUtil.getJedis();
        HashMap<String, String> map = new HashMap<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            //        计算购物车商品总价
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
        }
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart", map);
        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + userId + ":cart");
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }

        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
//        缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }
}
