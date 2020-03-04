package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsProductSaleAttrValue;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/29
 */
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;


    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo o = new PmsProductInfo();
        o.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(o);
        return pmsProductInfos;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr t = new PmsProductSaleAttr();
        t.setProductId(spuId);
        List<PmsProductSaleAttr> productSaleAttrs = pmsProductSaleAttrMapper.select(t);
        for (PmsProductSaleAttr pmsProductSaleAttr : productSaleAttrs
             ) {
            String productId = pmsProductSaleAttr.getProductId();
            PmsProductSaleAttrValue saleAttrValue = new PmsProductSaleAttrValue();
            saleAttrValue.setProductId(productId);
            saleAttrValue.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> productSaleAttrValues = pmsProductSaleAttrValueMapper.select(saleAttrValue);
            pmsProductSaleAttr.setPmsProductSaleAttrValueList(productSaleAttrValues);
        }
        return productSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage t = new PmsProductImage();
        t.setProductId(spuId);
        List<PmsProductImage> productImages = pmsProductImageMapper.select(t);
        return productImages;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        String productId = pmsProductInfo.getId();
//        添加销售属性
        List<PmsProductSaleAttr> productSaleAttrList = pmsProductInfo.getPmsProductSaleAttrList();
        for (PmsProductSaleAttr pmsProductSaleAttr:productSaleAttrList
             ) {
//            添加销售属性值
            String saleAttrId = pmsProductSaleAttr.getSaleAttrId();
            List<PmsProductSaleAttrValue> productSaleAttrValueList = pmsProductSaleAttr.getPmsProductSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue:productSaleAttrValueList
                 ) {
                pmsProductSaleAttrValue.setProductId(productId);
                pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
            pmsProductSaleAttr.setProductId(productId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
        }
//        添加图片
        List<PmsProductImage> productImageList = pmsProductInfo.getPmsProductImageList();
        for (PmsProductImage pmsProductImage:productImageList
             ) {
            pmsProductImage.setProductId(productId);
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }

        return "success";
    }
}
