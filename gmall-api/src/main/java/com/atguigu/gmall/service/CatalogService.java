package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PmsBaseCatalog1;
import com.atguigu.gmall.beans.PmsBaseCatalog2;
import com.atguigu.gmall.beans.PmsBaseCatalog3;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/28
 */
public interface CatalogService {
    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> PmsBaseCatalog2(String catalog1id);

    List<PmsBaseCatalog3> PmsBaseCatalog3(String catalog2id);
}
