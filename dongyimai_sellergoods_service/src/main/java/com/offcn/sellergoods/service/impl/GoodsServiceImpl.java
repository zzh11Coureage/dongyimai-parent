package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional //事务注解
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//1、插入商品基本信息表
		//设置商品状态 0 待审核
		goods.getGoods().setAuditStatus("0");
		goodsMapper.insert(goods.getGoods());

		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		//3、调用保存sku信息方法
		saveItemList(goods);


	}

	//抽取公共方法，保存sku
	public void saveItemList(Goods goods){
		//判断是否启用规格值为1，启用规格
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			//3、sku信息处理
			List<TbItem> itemList = goods.getItemList();
			//遍历sku集合
			for (TbItem item : itemList) {
				//获取spu商品标题
				String goodsName = goods.getGoods().getGoodsName();
				String jsonStr = item.getSpec();
				//转换规格的json字符串为集合
				Map specMap = JSON.parseObject(jsonStr, Map.class);
				//遍历规格map
				for (Object key : specMap.keySet()) {
					goodsName += " " + specMap.get(key);
				}
				//设置商品标题到sku对象
				item.setTitle(goodsName);

				//设置sku的属性值
				setItemValue(item,goods);

				//把sku数据保存到数据库
				itemMapper.insert(item);
			}
		}else {

			//创建一个skuduixiang
			TbItem item = new TbItem();
			//设置sku名称
			item.setTitle(goods.getGoods().getGoodsName());
			//设置商品那价格
			item.setPrice(goods.getGoods().getPrice());
			//设置一个默认库存
			item.setNum(99999);
			//设置是否默认
			item.setIsDefault("1");
			//设置是否启用
			item.setStatus("1");
			item.setSpec("{}");
			//调用设置sku属性方法
			setItemValue(item,goods);
			//保存sku到数据库
			itemMapper.insert(item);
		}
	}

	//提取设置sku属性值公用方法

	public void setItemValue(TbItem item,Goods goods){
		//设置spu商品编号
		item.setGoodsId(goods.getGoods().getId());
		//商家编号
		item.setSellerId(goods.getGoods().getSellerId());
		//商品分类编号
		item.setCategoryid(goods.getGoods().getCategory3Id());
		//sku商品创建时间
		item.setCreateTime(new Date());
		//更新时间
		item.setUpdateTime(new Date());
		//品牌名称

		//根据【】品牌id，读取对应品牌信息
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		if (brand != null) {
			//设置品牌名称到sku独享
			item.setBrand(brand.getName());
		}
		//分类名称
		//更加分类id，获取分类信息
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		if (itemCat != null) {
			//设置分类名称到sku对象
			item.setCategory(itemCat.getName());
		}

		//商家名称
		//根据商家编号，读取商家信息
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		if (seller != null) {
			item.setSeller(seller.getName());
		}

		//获取商品配图
		String itemImagesJsonStr = goods.getGoodsDesc().getItemImages();
		List<Map> imageslist = JSON.parseArray(itemImagesJsonStr, Map.class);
		//判断图片集合不为空，有内容，提取第一张图片
		if (imageslist != null && imageslist.size() > 0) {
			item.setImage((String) (imageslist.get(0).get("url")));
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改保存商品基本信息
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//修改保存商品扩展信息
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//删除sku信息
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		//3、调用保存sku信息方法
		saveItemList(goods);

	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		//根据商品编号，读取商品扩展信息
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		//处理sku
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//goodsMapper.deleteByPrimaryKey(id);
			//根据商品id，获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//设置商品 逻辑删除字段的属性值为 1
			goods.setIsDelete("1");
			//更新保存到数据库
			goodsMapper.updateByPrimaryKey(goods);
		}
       //修改sku商品状态为禁用
		List<TbItem> itemListByGoodsIdandStatus = findItemListByGoodsIdsStatus(ids, "1");
		for (TbItem listByGoodsIdandStatus : itemListByGoodsIdandStatus) {
			listByGoodsIdandStatus.setStatus("0");
			itemMapper.updateByPrimaryKey(listByGoodsIdandStatus);
		}

	}


	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();

		if(goods!=null){
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			//增加一个必须的过滤条件，不显示 删除标记字段为1的数据
			criteria.andIsDeleteIsNull();
		}

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] goodsIds, String status) {
		for(Long goodsId:goodsIds){
			//根据商品id获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			//修改商品状态
			goods.setAuditStatus(status);
			//更新商品信息到数据库
			goodsMapper.updateByPrimaryKey(goods);
			//修改sku的状态
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			List<TbItem> itemList = itemMapper.selectByExample(example);
			//遍历sku集合
			for(TbItem item:itemList){
				//修改状态
				item.setStatus("1");
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdsStatus(Long[] goodsIds, String status) {

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		//包含指定商品编号集合
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		//设置状态
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}


}
