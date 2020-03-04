package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/28
 */
@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    private PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    private PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    private PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> PmsBaseCatalog2(String catalog1id) {
        PmsBaseCatalog2 t = new PmsBaseCatalog2();
        t.setCatalog1Id(catalog1id);
        return pmsBaseCatalog2Mapper.select(t);
    }

    @Override
    public List<PmsBaseCatalog3> PmsBaseCatalog3(String catalog2id) {
        PmsBaseCatalog3 t = new PmsBaseCatalog3();
        t.setCatalog2Id(catalog2id);
        return pmsBaseCatalog3Mapper.select(t);
    }
}
