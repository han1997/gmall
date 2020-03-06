package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author hhy1997
 * 2020/3/4
 */
@Controller
@CrossOrigin
public class ItemController {
    @Reference
    private SpuService spuService;
    @Reference
    private SkuService skuService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId,
                       ModelMap map) {
//        sku
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        map.put("skuInfo", pmsSkuInfo);
//        销售属性
        List<PmsProductSaleAttr> productSaleAttrs = skuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), skuId);
        map.put("spuSaleAttrListCheckBySku", productSaleAttrs);
        return "item";
    }
}
