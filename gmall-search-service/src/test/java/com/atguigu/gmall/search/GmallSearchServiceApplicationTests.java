package com.atguigu.gmall.search;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SearchService;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Reference
    SkuService skuService;
    @Reference
    SearchService searchService;

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {
        PmsSearchParam pmsSearchParam = new PmsSearchParam();
        pmsSearchParam.setCatalog3Id("61");
        List<PmsSearchSkuInfo> searchSkuInfoList = searchService.list(pmsSearchParam);
        for (PmsSearchSkuInfo pmsSearchSkuInfo : searchSkuInfoList) {
            System.out.println(pmsSearchSkuInfo.getSkuName());
        }
    }
    @lombok.SneakyThrows
    @Test
    public void query() {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
//          jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("PmsSkuAttrValueList.valueId",51);
        boolQueryBuilder.filter(termQueryBuilder);
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","小米");
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        searchSourceBuilder.highlighter();

//        用api执行复杂的查询操作
        String query = searchSourceBuilder.toString();

        Search search = new Search.Builder(query).addIndex("gmallpms").addType("PmsSkuInfo").build();

        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }
        System.out.println(pmsSearchSkuInfos);

    }

    @Test
    public void input() throws IOException {
        //        获取sku
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSku();
//        转为es的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            try {
                BeanUtils.copyProperties(pmsSearchSkuInfo,pmsSkuInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }

//        输入skuinfo到es数据库
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            Index index = new Index.Builder(pmsSearchSkuInfo).index("gmallpms").type("PmsSkuInfo").id(String.valueOf(pmsSearchSkuInfo.getId())).build();
            jestClient.execute(index);
        }
    }

}
