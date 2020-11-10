package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

public interface CartService {


    public List<Cart> addGoodsToCartList(List<Cart> CartList, Long itemId, Integer num);


}
