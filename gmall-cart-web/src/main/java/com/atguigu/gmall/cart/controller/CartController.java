package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hhy1997
 * 2020/3/24
 */
@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;


    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, ModelMap modelMap,
                            HttpServletRequest request) {
        String memberId = (String) request.getAttribute("memberId");
//        调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);
//        获取最新缓存数据渲染给cartListInner
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList", omsCartItems);
        if (omsCartItems.size() > 0) {
//        获取购物车总价
            BigDecimal totalAmount = getTotalAmount(omsCartItems);
            modelMap.put("totalAmount", totalAmount);
        }

        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        System.out.println("cartList-->memberId:"+memberId);
        List<OmsCartItem> omsCartItems;
        if (StringUtils.isNotBlank(memberId)) {
//            用户已登录
            omsCartItems = cartService.cartList(memberId);
        } else {
//            用户未登录
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
        }

        modelMap.put("cartList", omsCartItems);
        if (omsCartItems != null && omsCartItems.size() > 0) {
//        获取购物车总价
            BigDecimal totalAmount = getTotalAmount(omsCartItems);
            modelMap.put("totalAmount", totalAmount);
        }
        return "cartList";
    }

    @RequestMapping("/toSuccess")
    public String toSuccess() {
        return "success";
    }

    @RequestMapping("/addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, Long quantity,
                            HttpServletRequest request, HttpServletResponse response) {
//        构建购物车
        List<OmsCartItem> omsCartItems = new ArrayList<>();
//        获取商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
//        将商品信息封装进购物车
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
//        判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");
        System.out.println(memberId);
        System.out.println(nickName);
        if (StringUtils.isBlank(memberId)) {
//              用户未登录
//                取出cookie的中的购物车
            String cookieValue = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cookieValue)) {
//                若cookie中没有购物车，直接把商品加入购物车
                omsCartItems.add(omsCartItem);
            } else {
//                cookie中有购物车
                omsCartItems = JSON.parseArray(cookieValue, OmsCartItem.class);
//                自写添加商品方法
                omsCartItems = addOmsCartItem(omsCartItems, omsCartItem);
                /*//判断购物车中是否已经存在该商品
                Boolean isExist = cart_isExist(omsCartItems, omsCartItem);
                if (isExist == false) {
//                不存在
                    omsCartItems.add(omsCartItem);
                } else {
//                存在,更新商品个数
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){

                        }
                    }
                }*/
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
        } else {
//           用户已登录

//            从数据库获取购物车信息
            OmsCartItem omsCartItemFromDB = cartService.ifCartExistByUser(memberId, skuId);

            if (omsCartItemFromDB == null) {
//                数据库没有该商品的购物车信息
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname(nickName);
                cartService.addCart(omsCartItem);
            } else {
//                数据库存在该商品的购物车信息，更新信息
                omsCartItemFromDB.setQuantity(omsCartItemFromDB.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDB);
            }

//            同步缓存
            cartService.flushCartCache(memberId);
        }


        return "redirect:/toSuccess";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
//            当商品被选中
            if ("1".equals(omsCartItem.getIsChecked())) {
                BigDecimal totalPrice = omsCartItem.getTotalPrice();
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }

    private List<OmsCartItem> addOmsCartItem(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        Boolean isExist = false;
        for (OmsCartItem cartItem : omsCartItems) {
//            判断购物车内是否存在该商品，若存在，修改数量
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                isExist = true;
            }
        }
        if (!isExist) {
//            不存在，添加商品进入购物车
            omsCartItems.add(omsCartItem);
        }
        return omsCartItems;
    }

    private Boolean cart_isExist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
//                购物车中存在该商品
                return true;
            }
        }
        return false;
    }

}
