app.controller("searchController",function ($scope,$location,searchService) {
   //搜索
    //添加搜索对象
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':30
    ,'sort':'','sortField':''};

    $scope.search=function () {
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
         searchService.search($scope.searchMap).success(function (response) {


         //搜索返回的结果封装集合
           $scope.resultMap=response;
          buildPageLabel();
         })
    }


    //添加搜索项
    $scope.addSearchItem=function (key,value) {
        //如果添加的是分类或品牌
           if (key=='category'|| key=='brand' ||key=='price'){
               $scope.searchMap[key]=value;
           }else {
               $scope.searchMap.spec[key]=value;
           }
           // 初始化当前页码为第一页

        $scope.searchMap.pageNo=1;
           //执行搜索
           $scope.search();
    }

    //移除符合搜索条件
    $scope.removeSearchItem=function (key) {
        //如果是分类或品牌
         if (key=='category' || key=='brand'|| key=='price'){
            $scope.searchMap[key]="";
         } else{
            // 否则是规格
             delete $scope.searchMap.spec[key];//移除此属性
         }
         $scope.search();
    }

    //构建分页标签
    buildPageLabel=function () {
        //新增分页栏属性
        $scope.pageLabel=[];
        //得到最终页码
        var maxPageNo=$scope.resultMap.totalPages;
        //定义开始页码
        var firstPage=1;
        //定义截止页码
        var lastPage=maxPageNo;
        //定义前面省略号
        $scope.firstDot=true;
        //定义后面省略号
        $scope.lastDot=true;

        //如果总页数大于5,显示部分页码
        if (maxPageNo>5){
            if ($scope.searchMap.pageNo<=3){
                lastPage=5;
                $scope.firstDot=false;
                //如果当前页数大于等于最大页码数-2
            }else if ($scope.searchMap.pageNo>=maxPageNo-2){
                firstPage=maxPageNo-4;
                $scope.lastDot=false;
            }else {
                //显示当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else{
            //当总页码不超过5页
            $scope.firstDot=false;
            $scope.lastDot=false;
        }

        //循环产生页码标签
        for ( var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
//跳转到指定页
    $scope.queryByPage=function (pageNo) {
          //页码验证
        if (pageNo<1 ||pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }

    //判断当前页为第一页
    $scope.isTopPage=function () {
           if ($scope.searchMap.pageNo==1){
               return true;
           }else{
               return  false;
           }
    }

    //定义返回结果，总页码
    $scope.resultMap={totalPages:1};
    //判断当前页码是否是最后一页
    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }
  //判断指定页码是否是当前页
    $scope.isPage=function (page) {
      if (parseInt(page)==parseInt($scope.searchMap.pageNo)){
          return true;
      }else{
          return false;
      }
    }

    //设置排序规则
    $scope.sortSearch=function (sortField,sort) {
         $scope.searchMap.sortField=sortField;
         $scope.searchMap.sort=sort;
         $scope.search();
    }

    //判断关键字是否是品牌
    $scope.keywordsIsBrand=function () {
            for (var i=0;i<$scope.resultMap.brandList.length;i++){
                //如果包含
                if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                    return true;
                }

            }
        return false;
    }
    //加载查询字符串
    $scope.loadkeywords=function () {
         $scope.searchMap.keywords=$location.search()['keywords'];
         $scope.search();
    }

})