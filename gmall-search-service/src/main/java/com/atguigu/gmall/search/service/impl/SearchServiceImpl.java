package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.beans.PmsSearchParam;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hhy1997
 * 2020/3/13
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private JestClient jestClient;

    /*
    es查询语句
    GET /gmallpms/PmsSkuInfo/_search
    {
      "query": {
        "bool": {
          "filter": [
            {
            "term": {
              "price": "1000"
                    }
            },
            {
              "term":{
                "PmsSkuAttrValueList.valueId":"51"
              }
          }],
          "must": [
            {
              "match": {
                "skuName": "小米"
                        }
            }
          ]
        }
      }
    }
    * */
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        String query = getSearchDsl(pmsSearchParam);

        Search search = new Search.Builder(query).addIndex("gmallpms").addType("PmsSkuInfo").build();

        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = null;
        try {
            hits = searchResult.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
                Map<String, List<String>> highlight = hit.highlight;
                if (highlight != null){
                    String skuName = highlight.get("skuName").get(0);
                    pmsSearchSkuInfo.setSkuName(skuName);
                }
                pmsSearchSkuInfos.add(pmsSearchSkuInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return pmsSearchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParam pmsSearchParam) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();

//        dsl语句构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        filter-----term---过滤
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("PmsSkuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
//        must----match--查询
        if (StringUtils.isNotBlank(catalog3Id)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
//        查询开始位置
        searchSourceBuilder.from(0);
//        查词条数
        searchSourceBuilder.size(20);
//        排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
//        高亮

        HighlightBuilder highlightBuilder = new HighlightBuilder();
//        自定义高亮
        highlightBuilder.preTags("<span style='color: red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

//        aggs
        /*TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("PmsSkuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);*/

//        用api执行复杂的查询操作
        return searchSourceBuilder.toString();
    }
}
