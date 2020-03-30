package com.atguigu.gmall.beans;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author handaxingyuner
 * @param
 * @return
 */

public class PmsProductInfo implements Serializable {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String productName;

    @Column
    private String description;

    @Column
    private  String catalog3Id;

    @Transient
    private List<PmsProductSaleAttr> pmsProductSaleAttrList;
    @Transient
    private List<PmsProductImage> pmsProductImageList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public List<PmsProductSaleAttr> getPmsProductSaleAttrList() {
        return pmsProductSaleAttrList;
    }

    public void setPmsProductSaleAttrList(List<PmsProductSaleAttr> pmsProductSaleAttrList) {
        this.pmsProductSaleAttrList = pmsProductSaleAttrList;
    }

    public List<PmsProductImage> getPmsProductImageList() {
        return pmsProductImageList;
    }

    public void setPmsProductImageList(List<PmsProductImage> pmsProductImageList) {
        this.pmsProductImageList = pmsProductImageList;
    }
}


