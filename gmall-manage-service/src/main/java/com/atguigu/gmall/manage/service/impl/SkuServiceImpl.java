package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.manage.util.RedisUtil;
import com.atguigu.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

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
        RedisUtil redisUtil = new RedisUtil();
        Jedis jedis = redisUtil.getJedis();
        System.out.println(jedis);

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
}
