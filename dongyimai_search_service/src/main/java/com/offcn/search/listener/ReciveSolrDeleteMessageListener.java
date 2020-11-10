package com.offcn.search.listener;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;
@Component
public class ReciveSolrDeleteMessageListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage= (ObjectMessage) message;
            try {
                Long[] ids= (Long[]) objectMessage.getObject();
                itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
