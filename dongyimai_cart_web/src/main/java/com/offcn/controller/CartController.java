package com.offcn.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.entity.Result;
import com.offcn.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    //查询购物车列表
    @RequestMapping("findCartList")
    public List<Cart>findCartList(){
        String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartList==null||cartList.equals("")){
            cartList="[]";
        }
        List<Cart> carts = JSON.parseArray(cartList, Cart.class);
        return carts;
    }
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        //获取购物车列表
        try {
            List<Cart> cartList = findCartList();
            cartList=cartService.addGoodsToCartList(cartList,itemId,num);
            CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }


    }
}
