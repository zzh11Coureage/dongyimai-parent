app.controller('itemController',function ($scope) {


    //调整购买数量方法
    $scope.addNum=function (x) {
        //定义一个变量存储购买数量
        $scope.num=$scope.num+x;

        //判断购买数量不能小于1
        if ($scope.num<1){
            $scope.num=1;
        }
    }

    //记录用户选择的规格
    $scope.specificationItems={};
    //用户选择规格
    $scope.selectSpecification=function (name,value) {
          $scope.specificationItems[name]=value;
          searchSku();
    }
    //判断某规格是否被选中
    $scope.isSelected=function (name,value) {
           if ($scope.specificationItems[name]==value){
               return true;
           }else {
               return  false;
           }
    }

    //加载默认SKU
    $scope.loadSku=function () {
          $scope.sku=skuList[0];
          $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //匹配两个对象
    matchObject=function (map1,map2) {
           for (var k in map1){
               if (map1[k]!=map2[k]){
                   return false;
               }
           }
        for (var k in map1){
            if (map2[k]!=map1[k]){
                return false;
            }
        }

    }

    //查询SKU
    searchSku=function(){
        for(var i=0;i<skuList.length;i++ ){
            if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
                $scope.sku=skuList[i];
                return ;
            }
        }
        $scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
    }

    //添加商品到购物车
    $scope.addToCart=function(){
        alert('skuid:'+$scope.sku.id);
    }

})