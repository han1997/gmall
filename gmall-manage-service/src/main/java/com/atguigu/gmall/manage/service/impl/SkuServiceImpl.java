package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.manage.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

/**
 * @author hhy1997
 * 2020/3/4
 */
@Service
public class SkuServiceImpl  implements SkuService {
    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
//        处理默认图片
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getPmsSkuImageList().get(0).getImgUrl());
        }
//        插入skuinfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();
//      插入平台属性
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getPmsSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList
             ) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
//        插入销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getPmsSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList
             ) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
//        插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getPmsSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList
             ) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        return "success";
    }

    @Override
    public PmsSkuInfo getSkuByIdFromDB(String skuId) {
//      获取sku基本信息
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
//        获取sku图片信息
        PmsSkuImage skuImage = new PmsSkuImage();
        skuImage.setSkuId(skuId);
        List<PmsSkuImage> skuImageList = pmsSkuImageMapper.select(skuImage);
        skuInfo.setPmsSkuImageList(skuImageList);
/*//        获取skuattrvalue信息(
//        先获取spu的attrId以及valueId
//         对应 baseAttr表的attrId
//         以及 baseAttrValue表的valueId)
        String productId = skuInfo.getProductId();
        PmsSkuAttrValue skuAttrValue = new PmsSkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<PmsSkuAttrValue> attrValueList = pmsSkuAttrValueMapper.select(skuAttrValue);
        skuInfo.setPmsSkuAttrValueList(attrValueList);*/
//        获取skusalesttrvalue信息
//       spu saleAttrValue 的 saleAttrId saleAttrValueId
//  对应  spu saleAttr
//       spu saleAttrValue
        PmsSkuSaleAttrValue saleAttrValue = new PmsSkuSaleAttrValue();
        saleAttrValue.setSkuId(skuId);
        List<PmsSkuSaleAttrValue> saleAttrValueList = pmsSkuSaleAttrValueMapper.select(saleAttrValue);
        skuInfo.setPmsSkuSaleAttrValueList(saleAttrValueList);

        return skuInfo;
    }
    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
//        连接缓存
        Jedis jedis = redisUtil.getJedis();
//        查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuJson = jedis.get(skuKey);
        if (StringUtils.isNotBlank(skuJson)){
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        }else {
//        缓存查不到，缓存查询sql
            //作为标识码防止误删其他客户端的锁
            String token = UUID.randomUUID().toString();
//            设置分布式锁
            String OK = jedis.set("sku:" + skuId + ":lock", token, "NX", "PX", 10 * 1000);
            if (StringUtils.isNotBlank(OK) && "OK".equals(OK)){
                pmsSkuInfo = getSkuByIdFromDB(skuId);
//        sql返回结果给客户端并将结果写入缓存
                if (pmsSkuInfo != null){
                    jedis.set("sku:" + skuId + ":info",JSON.toJSONString(pmsSkuInfo));
                }else {
//                sql也查不到，防止缓存穿透，设置null或者""
                    jedis.setex("sku:" + skuId + ":info",60*3,JSON.toJSONString(""));
                }
//                验证是否是自己的锁
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                if (StringUtils.isNotBlank(lockToken) && lockToken.equals(token)){
//                去除分布式锁
                    jedis.del("sku:" + skuId + ":lock");
                }
//              防止在确认自己锁的瞬间，自己的锁失效，使用lua脚本
//                String script = "";
//                jedis.eval(script,)
            }else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }

        }
        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId ,String skuId) {
        List<PmsProductSaleAttr> productSaleAttrList = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
        return productSaleAttrList;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> skuInfoList = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return skuInfoList;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue t = new PmsSkuAttrValue();
            t.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(t);
            pmsSkuInfo.setPmsSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfoList;
    }
}
