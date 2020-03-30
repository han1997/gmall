package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PmsSearchParam;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;

import java.util.List;

/**
 * @author hhy1997
 * 2020/3/13
 */
public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
