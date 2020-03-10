package com.atguigu.gmall.manage;

import com.atguigu.gmall.manage.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void contextLoads(){
        Jedis jedis = redisUtil.getJedis();
        System.out.println(jedis.get("hhy"));
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        lock.unlock();
    }

}
