package com.atguigu.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.sun.deploy.net.HttpUtils;
import org.apache.http.client.HttpClient;
import util.HttpClientUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/26
 */
public class testOauth {
    public static void main(String[] args) {
//        String s = HttpClientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1565861648&response_type=code&redirect_uri=http://127.0.0.1:8085/vlogin");
//        code = eeb41f46919493c3eb5287495c36c18b
/*        String s2 = "https://api.weibo.com/oauth2/access_token";
        Map<String, String> map = new HashMap<>();
        map.put("client_id","1565861648");
        map.put("client_secret","d46d195cf435d955f4e7cce42f7a476e");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://127.0.0.1:8085/vlogin");
        map.put("code","be1db3b11b80d198b466f1e8dcdab9cb");
        String s1 = HttpClientUtil.doPost(s2,map);
        System.out.println(s1);*/
//        s1 = {"access_token":"2.00LJqIOGCJMyhB86d95e3d43pCC9yB","remind_in":"157679999","expires_in":157679999,"uid":"5705772777","isRealName":"true"}
        String s3 = "https://api.weibo.com/2/users/show.json?access_token=2.00LJqIOGCJMyhB86d95e3d43pCC9yB&uid=5705772777";
        String s = HttpClientUtil.doGet(s3);
        Map<String, String> map = JSON.parseObject(s, Map.class);
        System.out.println(map);
    }
}
