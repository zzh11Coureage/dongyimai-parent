package com.offcn.page.listener;

import com.offcn.entity.PageResult;
import com.offcn.page.service.impl.ItemPageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
@Component
public class ReciveCreatePageMessageListener implements MessageListener {

    @Autowired
    private ItemPageServiceImpl itemPageService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage= (ObjectMessage) message;
            try {
                Long[] goodsIds= (Long[]) objectMessage.getObject();
                for (Long goodsId : goodsIds) {
                    itemPageService.genItemHtml(goodsId);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
