package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
//import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;

/**
 * 商品controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

//	@Reference(timeout = 40000)
//	private ItemPageService itemPageService;

	@Autowired
	private Destination dongyimai_queue_solr;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination dongyimai_queue_delete_solr;

	//注入生成静态页面的消息主题
	@Autowired
	private Destination dongyimai_topic_page;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
             jmsTemplate.send(dongyimai_queue_delete_solr, new MessageCreator() {
				 @Override
				 public Message createMessage(Session session) throws JMSException {
					 return session.createObjectMessage(ids);
				 }
			 });
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 *
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	/**
	 * 更新状态
	 * @param
	 * @param status
	 */
	//修改商品状态
	@RequestMapping("updateStatus")
	public Result updateStatus(Long[] goodsIds,String status){
		try {
			goodsService.updateStatus(goodsIds, status);
			//判断审核状态是否是 审核通过
			if(status!=null&&status.equals("1")){
				//1、调用商家商品服务，获取审核通过商品编号对应的sku集合
				List<TbItem> itemList = goodsService.findItemListByGoodsIdsStatus(goodsIds, status);
			//调用jmsTemplate,发送消息到中间件

				final String jsonString = JSON.toJSONString(itemList);
				//调用JmsTemplate，发送消息
				jmsTemplate.send(dongyimai_queue_solr, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(jsonString);
					}
				});


				//3、调用静态页面生成服务，生成对应的静态页面
//				for (Long goodsId : goodsIds) {
//					itemPageService.genItemHtml(goodsId);
//				}
				//发送消息到生成静态页面主题,
            jmsTemplate.send(dongyimai_topic_page, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(goodsIds);
				}
			});

			}
			return new Result(true,"修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"修改状态失败");
		}
	}

////	生成静态页测试
//	@RequestMapping("/genHtml")
//	public void genHtml(Long goodsId){
//		boolean b = itemPageService.genItemHtml(goodsId);
//		System.out.println("page:  "+b);
//	}


}
