package com.offcn.sms.listener;


import com.offcn.sms.util.smsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
@Component
public class ReciveSmsSendMsgListener implements MessageListener {

    @Autowired
    private smsTemplate smsTemplate;


    public void onMessage(Message message) {

        if(message instanceof MapMessage){
            MapMessage mapMessage= (MapMessage) message;
            //获取手机号码
            try {
                String mobile = mapMessage.getString("mobile");
                String smscode = mapMessage.getString("smscode");
                System.out.println("收到短信发送请求:"+mobile+" "+smscode);
                //调用短信发送服务，发送短信
                smsTemplate.sendSms(mobile,smscode);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
