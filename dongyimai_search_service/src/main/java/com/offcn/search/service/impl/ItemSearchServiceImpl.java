package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import com.alibaba.dubbo.config.annotation.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object>map=new HashMap<>();
//        //创建查询器对象
//        Query query = new SimpleQuery();
//        //创建查询条件
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        //关联查询
//       query.addCriteria(criteria);
//        //发出查询请求
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//       //提取查询结果到集合
//      List<TbItem>content= page.getContent();
//     //把数据结果封装到map
//       map.put("rows",content);
        //判断查询的关键字是否为空，不为空，就要判断是否带空格
        //移除搜索关键字里面的空格

        String keywords=(String)searchMap.get("keywords");
        if(keywords!=null&&keywords.indexOf(" ")>=0){
            //移除空格
            keywords=keywords.replaceAll(" ","");
            searchMap.put("keywords",keywords);
        }


    map.putAll(searchList(searchMap));
//        //return map;
        List categoryList = searchCategoryList(searchMap);

        if ("".equals(searchMap.get("category"))){
            //提取第一个分类
            if(categoryList !=null && categoryList.size()>0){
                map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
            }else{
                map.putAll(searchBrandAndSpecList((String)searchMap.get("category")));
            }
        }

        //封装集合到map
        map.put("categoryList",categoryList);
        return map;
    }

    @Override
    public void importList(List<TbItem> list) {
        for (TbItem item : list) {


            //从数据库中提取规格json字符串转化为map
            Map<String,String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            Map<String,String> map=new HashMap<>();
            for (String key : specMap.keySet()) {
                map.put(Pinyin.toPinyin(key,"").toLowerCase().trim(), specMap.get(key));
            }
            //给带动态域注解的字段赋值
            item.setSpecMap(map);
            System.out.println("导入成功"+list.size());
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdsList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdsList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //根据关键字查询,显示高亮
    private Map searchList(Map searchMap){
        Map map = new HashMap<>();

        //1.创建一个支持高亮的查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //创建查询条件
      Criteria criteria=  new Criteria("item_keywords").is(searchMap.get("keywords"));
      //关联查询条件到高亮显示器对象
        query.addCriteria(criteria);
        //判断条件1 按照分类进行过滤
        //判断分类查询过滤条件是否不为空白
        if (!"".equals(searchMap.get("category"))){
            //创建一个查询条件
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            //创建一个过滤器对象
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            //关联过滤器对象到查询器对象
            query.addFilterQuery(filterQuery);
        }
        //过滤条件2,按照品牌进行过滤
        //判断品牌条件是否为空白
        if (!"".equals(searchMap.get("brand"))){
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //过滤条件3 按照规格和规格选项进行过滤
        if (searchMap.get("spec")!=null){
            //获取规格对象
            Map<String,String>specMap= (Map<String, String>) searchMap.get("spec");
            //遍历规格map
            for (String key : specMap.keySet()) {
                //创建查询条件
                //创建查询条件
                Criteria criteriaSpec = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase().trim()).is(specMap.get(key));
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaSpec);
                query.addFilterQuery(filterQuery);
            }
            //过滤条件4 按价格筛选
            if(!"".equals(searchMap.get("price"))){
                String[] price = ((String) searchMap.get("price")).split("-");
               //如果区间起点不等于0
                if (!price[0].equals("0")){
                    Criteria criteria1 = new Criteria("item_price").greaterThanEqual(price[0]);
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                    query.addFilterQuery(filterQuery);
                    //如果区间终点不定*
                }if (!price[1].equals("*")){
                    Criteria criteria2 = new Criteria("item_price").lessThan(price[1]);
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteria2);
                    query.addFilterQuery(filterQuery);
                }
            }

            //分页查询
            //提取页码
            Integer pageNo= (Integer) searchMap.get("pageNo");
            //判断当前页码是否为空
            if (pageNo==null){
//                默认为第一页
                pageNo=1;
            }
            //参数2每页记录数
            Integer pageSize= (Integer) searchMap.get("pageSize");
            if (pageSize==null){
                //设置默认每页记录数为30
                pageSize=30;
            }
          // 设置分页属性对象
            //计算游标开始位置
            int start=(pageNo-1)*pageSize;
            query.setOffset(start);
            query.setRows(pageSize);

        }
      //设置排序方式
        //接收前端传递过来的排序方式sort
      String sortValue= (String) searchMap.get("sort");
        //接收前端传来的排序字段
        String sortField= (String) searchMap.get("sortField");
        //判断排序方式不为空,并且排序方式为ASC升序
        if (sortValue!=null && !sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sortASC = new Sort(Sort.Direction.ASC, "item_"+sortField);
                System.out.println("升序对象"+sortValue+"排序字段"+sortField);
                query.addSort(sortASC);
            }if (sortValue.equals("DESC")){
                Sort sortDESC = new Sort(Sort.Direction.DESC, "item_"+sortField);
                System.out.println("降序对象"+sortValue+"降序字段"+sortField);
                query.addSort(sortDESC);

            }
        }






        //2.设定需要高亮的字段
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置标题为高亮
        highlightOptions.addField("item_title");
        //3.设置高亮的前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4.设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5.关联高亮选项到查询器对象
        query.setHighlightOptions(highlightOptions);
        //6.设定查询条件,根据关键字查询
        Criteria criteria11 = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria11);
        //7.发出高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //8.获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9.遍历高亮集合
        for (HighlightEntry<TbItem> hlightEntry : highlightEntryList) {
            //获取基本数据对象
            TbItem tbItem = hlightEntry.getEntity();
            if (hlightEntry.getHighlights().size()>0&&hlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                List<HighlightEntry.Highlight>highlightList=hlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果,设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }
//把高亮数据集合存放到map
       List<TbItem> content= page.getContent();
        map.put("rows",content);
//把查询到的总记录数封装到map
map.put("total",page.getTotalElements());
map.put("totalPages",page.getTotalPages());
        return map;
    }

    //查询分类列表
    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList();
        Query query = new SimpleQuery();

        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

//        设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            //将分组结果的名称封装到返回值中
            list.add(entry.getGroupValue());

        }
        return list;
    }

    //根据分类名称，获取对应 品牌和规格规格选项数据
    private Map searchBrandAndSpecList(String categoryName){
        Map map=new HashMap();


        //1、根据分类名称，去redis读取该分类名称对应的模板编号
        Long typeTempleId= (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);

        //判断如果读取到模板编号
        if(typeTempleId!=null){

            //2、根据模板编号，获取对应品牌 从redis读取
            List brandList= (List) redisTemplate.boundHashOps("brandList").get(typeTempleId);
            //判断从缓存读取到品牌数据
            if(brandList!=null){
                //把读取到品牌数据封装map
                map.put("brandList",brandList);
            }else {
                System.out.println("从缓存读取模板编号是:"+typeTempleId+" 品牌数据失败");
            }

            //3、根据模板编号，获取对应规格和规格选项数据
            List specList= (List) redisTemplate.boundHashOps("specList").get(typeTempleId);
            if(specList!=null){
                //把读取到规格和规格选项数据封装到map
                map.put("specList",specList);
            }else {
                System.err.println("从缓存读取模板编号是:"+typeTempleId+" 规格数据失败");
            }
        }else {
            System.err.println("从缓存读取模板编号是:"+typeTempleId+" 模板数据失败");
        }

        return map;
    }

    //根据关键字搜索列表

}
