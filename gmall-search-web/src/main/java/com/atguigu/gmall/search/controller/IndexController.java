package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @author hhy1997
 * 2020/3/13
 */
@Controller
public class IndexController {
    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;


    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam,
                       ModelMap map) {
        List<PmsSearchSkuInfo> skuLsInfoList = searchService.list(pmsSearchParam);
        map.put("skuLsInfoList", skuLsInfoList);
        Set<String> set = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : skuLsInfoList) {
            List<PmsSkuAttrValue> pmsSkuAttrValueList = pmsSearchSkuInfo.getPmsSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                set.add(valueId);
            }
        }
//       根据valueId查询属性列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getAttrValueListByValueId(set);
        map.put("attrList", pmsBaseAttrInfoList);

//        去除已经查询过的valuId
        String[] delValueIds = pmsSearchParam.getValueId();
        if (delValueIds != null) {
            //      面包屑
            ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();

            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, delValueId));

                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            //面包屑查找属性名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            map.put("attrValueSelectedList", pmsSearchCrumbs);

        }

        //地址参数
        String urlParam1 = getUrlParam(pmsSearchParam);
        map.put("urlParam", urlParam1);
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            map.put("keyword", keyword);
        }

        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String delValueId) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        String urlParam = "";
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;

        }
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                if (!valueId.equals(delValueId)) {
                    urlParam = urlParam + "&valueId=" + valueId;
                }
            }
        }
        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        String urlParam = "";
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;

        }
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                urlParam = urlParam + "&valueId=" + valueId;
            }
        }
        return urlParam;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index() {
        return "index";
    }
}
