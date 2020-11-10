 //商品控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,uploadService,itemCatService,
  typeTemplateService	,$location									   ){
	
	$controller('baseController',{$scope:$scope});//继承

	// 定义商品组合json对象
	$scope.entity={"goos":{"isEnableSpec":0},"goodsDesc":{"itemImages":[],"specificationItems":[]}};

  //定义商品状态
	$scope.status=['未审核','已审核','审核未通过','关闭'];

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
		var id=$location.search()['id'];
		if (id==null){
			return;
		}

		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages)
				//读取扩展属性，转换为json对象
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //读取用户选中的规格,转化为json对象
	            $scope.entity.goodsDesc.specificationItems=JSON.parse( $scope.entity.goodsDesc.specificationItems);

	            //SKU列表规格转换
				for (var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec)
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		//提取富文本编辑器的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){

					alert('保存成功')


				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//保存
	$scope.add=function(){
		//读取富文本编辑器内容
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert('保存成功');
					//清空entity对象
					$scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};
					//清空富文本编辑器内容
					editor.html('');
				}else{
					alert(response.message);
				}
			}
		);
	}
	//图片上传处理方法
	$scope.upload=function () {
		uploadService.upload().success(function (response) {
			//判断上传是否成功
			if(response.success){
				alert("图片上传成功");
				$scope.image_entity.url=response.message;
			}
		})
	}

	//当上传图片成功后，点击保存按钮，保存图片信息到图片集合
	$scope.entity={goods:{},goodsDesc:{itemImages:[]}};//定义页面实体结构
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	//删除图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}


	//读取一级分类
	$scope.selectItemCat1List=function () {
       itemCatService.findByParentId(0).success(function (response) {
           $scope.itemCat1List=response;
	   })
	}

	//读取二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
         //判断一级分类的值去选择二级分类
		if(newValue){
			itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat2List=response;
			})
		}
	})

	// 读取三级分类
        $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        	//判断二级分类的值
			if(newValue){
				itemCatService.findByParentId(newValue).success(function (response) {
					$scope.itemCat3List=response;

				})
			}

		})
//三级分类后 读取模板id
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
       if(newValue){
       	//获取该分类id对应分类信息
       	itemCatService.findOne(newValue).success(function (response) {
       		//更新模板id
             $scope.entity.goods.typeTemplateId=response.typeId;
		})
	   }
	})

//模板id选择后 更新品牌列表
$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
	if(newValue){
		typeTemplateService.findOne(newValue).success(function (response) {
			//获取类型模板
          $scope.typeTemplate=response;
          //品牌列表
          $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
        //把模板信息扩展属性值,转化为json对象
			//判断id为空,执行本行
			if ($location.search()['id']==null) {
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems)
			}
		 //查询规格列表
			typeTemplateService.findSpecList(newValue).success(function (response) {
                  $scope.specList=response;
			})

		})

	}

})
	  $scope.entity={goodsDesc: {itemImages: [],specificationItems:[]}};
     //当用户点击 对应规格选项 复选框的时候调用本方法,记录用户选中回个和规格选项数据
	//name 规格名称  value:规格选项
	$scope.updateSpecAttribute=function ($event,name,value) {
       //根据规格名称搜索 记录用户选中规格选项数组,看看是否存在记录
		var object=$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
         //如果规格已经存在
		if (object!=null){
			//判断复选框状态
			 if ($event.target.checked){
			 	object.attributeValue.push(value);
			 }else {
			 	 var index=object.attributeValue.indexOf(value);
				 object.attributeValue.splice(index,1);
				 //判断规格选项数组,等于0,移除整个规格对象
				 if (object.attributeValue.length==0){
				 	var index2=$scope.entity.goodsDesc.specificationItems.indexOf(object)
				 	$scope.entity.goodsDesc.specificationItems.splice(index2,1);
				 }

			 }
		}else {
			//第一次选中,抽取数据到json数组
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
//当用户选中、取消选中 规格选项的时候调用本方法，组装sku列表
	$scope.createItemList=function () {
		//定义一个数组，存储sku列表
		$scope.entity.itemList=[{"price":0,"num":0,"status":0,"isDefault":0,spec:{}}];
		//读取记录用户选中规格、规格选项对象
		var items=	$scope.entity.goodsDesc.specificationItems;

		//循环遍历用户选中规格、规格选项集合
		for(var i=0;i<items.length;i++){
			$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}

	}

	//定义扩充sku列表的列方法
	addColumn=function (itemList,attributeName,attributeValue) {
		//定义一个新数组，存储扩充后sku数据
		var newList=[];
		//遍历sku列表
		for (var i=0;i<itemList.length;i++){

			var oldRow=	itemList[i];
			//遍历规格选项数组
			for(var j=0;j<attributeValue.length;j++){
				//深克隆一个对象
				var newRow=	JSON.parse(JSON.stringify(oldRow));
				//扩充规格列
				newRow.spec[attributeName]=attributeValue[j];
				//添加到newList
				newList.push(newRow);
			}
		}

		return newList;
	}
	$scope.itemCatList=[];//商品分类列表
//加载商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
			function(response){
				for(var i=0;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		);
	}
//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}
});	