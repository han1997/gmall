package com.atguigu.gmall.bean;

/**
 * @author hhy1997
 * 2020/3/13
 */

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PmsSearchParam implements Serializable {
    private String catalog3Id;
    private String keyword;
    private String[] valueId;
}
