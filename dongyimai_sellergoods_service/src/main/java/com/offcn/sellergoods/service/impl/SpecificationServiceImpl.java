package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.Map;

/**
 * 规格服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	//引入规格选项数据操作接口
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}



	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//保存修改的规格
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        //删除原有规格
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());
		//指定规格id为条件
		criteria.andIdEqualTo(specification.getSpecification().getId());
		specificationOptionMapper.deleteByExample(example);//删除
		//循环插入规格选项
		for(TbSpecificationOption specificationOption:specification.getSpecificationOptionList()){
			specificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(specificationOption);
		}


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */

	@Override
	public Specification findOne(Long id){
		//查询规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//查询规格选项列表
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);//根据规格ID查询
		List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
		//构建组合实体类返回结果
		Specification specification=new Specification();
		specification.setSpecification(tbSpecification);
		specification.setSpecificationOptionList(optionList);
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
			//删除原有的规格选项
			TbSpecificationOptionExample example=new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

	@Override
	public void add(Specification specification) {
		//插入规格
		specificationMapper.insert(specification.getSpecification());
		//循环插入规格选项
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption specificationOption : specificationOptionList) {
			//关联规格和规格选项
			specificationOption.setSpecId(specification.getSpecification().getId());
              specificationOptionMapper.insert(specificationOption);
		}
	}


}
