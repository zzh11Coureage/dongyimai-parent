package com.offcn.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemPageServiceImpl implements ItemPageService {
//注入spring声明的配置对象
    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
        Configuration configuration = freeMarkerConfig.getConfiguration();
        Template template = configuration.getTemplate("item.ftl");
        Map dataModel = new HashMap();
        //加载商品表数据
        TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goods",goods);
        //加载商品扩展数据
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goodsDesc",goodsDesc);
        //商品分类
            String itemcat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemcat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemcat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
   dataModel.put("itemcat1",itemcat1);
            dataModel.put("itemcat2",itemcat2);
            dataModel.put("itemcat3",itemcat3);
         //sku列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            //状态为有效
            criteria.andStatusEqualTo("1");
            //指定id
            criteria.andGoodsIdEqualTo(goodsId);
            //按照状态降序,保证第一个为默认
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);



            Writer writer = new FileWriter(pagedir + goodsId + ".html");
            template.process(dataModel,writer);
            writer.close();
            System.out.println("商品编号为:"+goodsId+"的静态页面生成");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
}
