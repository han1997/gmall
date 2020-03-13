package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;

import java.util.List;

/**
 * @author handaxingyuner
 */
public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuByIdFromDB(String skuId);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku();
}
