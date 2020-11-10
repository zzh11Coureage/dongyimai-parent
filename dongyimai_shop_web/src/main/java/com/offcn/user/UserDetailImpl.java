package com.offcn.user;

import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建一个集合，存储用户角色
        List<GrantedAuthority> list=new ArrayList<GrantedAuthority>();
        //向集合添加指定角色
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        //根据用户账号，获取商家用户信息
        TbSeller seller = sellerService.findOne(username);
        //判断商家用户信息是否为空
        if(seller!=null){
            //判断商家状态是否审核通过
            if(seller.getStatus().equals("1")){
                //返回认证对象
                return new User(username,seller.getPassword(),list);
            }else {
                System.out.println("登录失败,商家状态:"+seller.getStatus());
                return null;
            }
        }else {
            return null;
        }


    }
}
