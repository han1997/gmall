package com.atguigu.gmall.beans;

/**
 * @author hhy1997
 * 2020/3/13
 */

import lombok.Data;

import java.io.Serializable;

@Data
public class PmsSearchParam implements Serializable {
    private String catalog3Id;
    private String keyword;
    private String[] valueId;
}
