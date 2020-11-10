package com.offcn.sms.util;

import com.offcn.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class smsTemplate {


    @Value("${appcode}")
    private String appcode;
    @Value("${tpl_id}")
    private String tpl_id;

    //定义接收短信的网关地址
    private String host="http://dingxin.market.alicloudapi.com";


    //定义短信发送方法
    public String sendSms(String mobile,String smscode){
        //短信发送接口路径
        String path="/dx/sendSms";
        String method="POST";
        //创建请求头集合
        Map<String,String> heads=new HashMap<String, String>();
        heads.put("Authorization","APPCODE "+appcode);
        //创建请求参数集合
        Map<String,String> querys=new HashMap<String, String>();
        //设置要接收手机号码
        querys.put("mobile",mobile);
        //设置短信验证码值
        querys.put("param","code:"+smscode);
        //设置短信发送模板编号
        querys.put("tpl_id",tpl_id);

        //创建请求体对象
        Map<String,String> bodys=new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, heads, querys, bodys);

            System.out.println("响应结果:"+response.toString());

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "短信发送出错:"+e.getMessage();
        }
    }
}
