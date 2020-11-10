package com.offcn;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private TbItemMapper itemMapper;

    //编写读取数据sku商品信息，导入到solr方法
    public void importSolr(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);

        for (TbItem item : itemList) {
            System.out.println("商品名称:"+item.getTitle());
            //读取规格属性值 {"机身内存":"16G","网络":"联通3G"}
            String specJson = item.getSpec();
            Map<String,String> specMap = JSON.parseObject(specJson, Map.class);
            //新建一个map，key是拼音
            Map<String,String> specMapPY=new HashMap<String, String>();

            //遍历汉子的map
            for (String key : specMap.keySet()) {
                specMapPY.put(Pinyin.toPinyin(key,"").toLowerCase().trim(),specMap.get(key));
            }


            //关联规格集合到实体规格动态域属性
            item.setSpecMap(specMapPY);
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

        System.out.println("导入solr成功");
    }


    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
     SolrUtil solrUtil= (SolrUtil) context.getBean("solrUtil");
     //调用导入方法
        solrUtil.importSolr();
    }
}
