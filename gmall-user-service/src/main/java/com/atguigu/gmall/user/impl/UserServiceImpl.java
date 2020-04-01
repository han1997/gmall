package com.atguigu.gmall.user.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;
import com.atguigu.gmall.manage.util.RedisUtil;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/22
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Override
    public List<UmsMember> list() {
//        UmsMemberExample umsMemberExample = new UmsMemberExample();
//        umsMemberExample.setOrderByClause("id asc");
//        return umsMemberMapper.selectByExample(umsMemberExample);
//        return umsMemberMapper.selectAllUser();
        return umsMemberMapper.selectAll();
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String userInfoStr = jedis.get("user:" + umsMember.getUsername()
                        + ":" + umsMember.getPassword() + ":userInfo");
                if (StringUtils.isNotBlank(userInfoStr)) {
                    //            登录成功
                    UmsMember umsMemberFromCache = JSON.parseObject(userInfoStr, UmsMember.class);
                    System.out.println(umsMemberFromCache.getNickname());
                    return umsMemberFromCache;
                }
//                redis加载失败，直接查询数据库
                //            登录失败
                //                1.密码错误
                //                2.缓存没有 ,查数据库
                UmsMember umsMemberFromDb = umsMemberMapper.selectOne(umsMember);
                String umsMemberToCache = JSON.toJSONString(umsMemberFromDb);
                if (umsMemberFromDb != null) {
                    jedis.setex("user:" + umsMember.getUsername()
                                    + ":" + umsMember.getPassword() + ":userInfo",
                            60 * 60 * 24, umsMemberToCache);
                }
                System.out.println(umsMemberFromDb.getNickname());
                return umsMemberFromDb;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:" + memberId + ":token", 60 * 60 * 2, token);
        jedis.close();
    }


    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {

        umsMemberMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(UmsMember checkUser) {
        UmsMember umsMember = umsMemberMapper.selectOne(checkUser);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddressListByMemberId(String memberId) {
        UmsMemberReceiveAddress t = new UmsMemberReceiveAddress();
        t.setMemberId(memberId);
        List<UmsMemberReceiveAddress> addressList = umsMemberReceiveAddressMapper.select(t);
        return addressList;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress t = new UmsMemberReceiveAddress();
        t.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress = umsMemberReceiveAddressMapper.selectOne(t);
        return umsMemberReceiveAddress;
    }
}
