package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author hhy1997
 * 2020/3/13
 */
@Controller
public class IndexController {
    @Reference
    private SearchService searchService;


    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam,
                       ModelMap map){
        List<PmsSearchSkuInfo> skuLsInfoList = searchService.list(pmsSearchParam);
        map.put("skuLsInfoList",skuLsInfoList);
        return "list";
    }

    @RequestMapping("index")
    public String index(){
        return "index";
    }
}
